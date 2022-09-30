package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import forex.domain.{ Rate }
import scala.concurrent.duration._
import forex.config._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats

import java.time.OffsetDateTime

import scalaj.http.{Http}

class Info(var pairObj: Rate.Pair, var timeObj: OffsetDateTime,  var priceObj: BigDecimal )

class OneFrameLive[F[_]: Applicative]() extends Algebra[F] {
  
  val Memo = new scala.collection.mutable.HashMap[Rate.Pair, Info]

  override def get(pair: Rate.Pair) : F[Error Either Rate] =
  {
    //Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]
    
    if (Memo.contains(pair)){
      implicit val formats = DefaultFormats
      
      val tempinfo = Memo.get(pair)
      //val price = Memo.get(pair)(priceObj)
      
      val time = tempinfo.map(_.timeObj).get
      val price = tempinfo.map(_.priceObj).get
      
      val offset = OffsetDateTime.now()
      val timeDif = Duration(offset.toEpochSecond - time.toEpochSecond, SECONDS)
      
      if (timeDif <= 300.second){
        Rate(pair, price, time).asRight[Error].pure[F]
      }
      else{

        //config <- Config.stream("app")
        //val config = ConfigSource.resources("app").load[ApplicationConfig]
        val config = ConfigSource.default.at("app").loadOrThrow[ApplicationConfig]
        val response = Http("localhost:8080/rates")
          .param("pair", pair.from.toString + pair.to.toString)
          .headers(Seq("Authorization" -> ("token: " + config.http.api_key), "Accept" -> "application/json"))
          .asString
          .body
        
        //not found: value parse
        val jsonRequest = parse(response)
        
        //val retPrice = jsonRequest.extract[Price]
        val retPrice = (jsonRequest \ "price").extract[String]
        Memo += pair -> (Price(BigDecimal(retPrice.toInt)), Timestamp.now)
        Rate(pair, Price(BigDecimal(retPrice.toInt)), Timestamp.now).asRight[Error].pure[F]
      }
    }
    else{
        val config = ConfigSource.default.at("app").loadOrThrow[ApplicationConfig]
        val response = Http("localhost:8080/rates")
          .param("pair", pair.from.toString + pair.to.toString)
          .headers(Seq("Authorization" -> ("token: " + config.http.api_key), "Accept" -> "application/json"))
          .asString
          .body
        
        //not found: value parse
        val jsonRequest = parse(response)
        
        //val retPrice = jsonRequest.extract[Price]
        val retPrice = (jsonRequest \ "price").extract[String]
        Memo += pair -> (Price(BigDecimal(retPrice.toInt)), Timestamp.now)
        Rate(pair, Price(BigDecimal(retPrice.toInt)), Timestamp.now).asRight[Error].pure[F]
    }
  }
}
