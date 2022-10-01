package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import forex.domain.{ Rate, Price, Timestamp }
import scala.concurrent.duration._
import forex.config._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import cats._
import implicits._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats

import java.time.OffsetDateTime
import forex.services.rates.errors.Error

import scalaj.http.{Http}

class Info(var pairObj: Rate.Pair, var priceObj: BigDecimal, var timeObj: Timestamp )

class OneFrameLive[F[_]: Applicative]() extends Algebra[F] {
  
  implicit val formats = DefaultFormats
  val Memo = new scala.collection.mutable.HashMap[Rate.Pair, Info]

  override def get(pair: Rate.Pair) : F[Error Either Rate] =
  {
    //Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]
    
    if (Memo.contains(pair)){
      
      
      val tempinfo = Memo.get(pair)
      //val price = Memo.get(pair)(priceObj)
      
      val time = tempinfo.map(_.timeObj).get
      val price = tempinfo.map(_.priceObj).get
      
      val offset = OffsetDateTime.now()
      val timeDif = Duration(offset.toEpochSecond - time.value.toEpochSecond, SECONDS)
      
      if (timeDif <= 300.second){
        Rate(pair, Price(price), time).asRight[Error].pure[F]
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
        val infoToSave = new Info(pair, BigDecimal(retPrice.toInt), Timestamp.now  )
        Memo += pair -> infoToSave
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
        val infoToSave = new Info(pair, BigDecimal(retPrice.toInt), Timestamp.now )
        Memo += pair -> infoToSave
        Rate(pair, Price(BigDecimal(retPrice.toInt)), Timestamp.now).asRight[Error].pure[F]
    }
  }
}
