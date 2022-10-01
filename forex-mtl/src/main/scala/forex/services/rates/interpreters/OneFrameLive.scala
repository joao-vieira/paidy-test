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
  
  def requestFromAPI(pair:Rate.Pair) : Info =
  {
      val config = ConfigSource.default.at("app").loadOrThrow[ApplicationConfig]
      val response = Http("http://0.0.0.0:8080/rates")
        .param("pair", pair.from.toString + pair.to.toString)
        .header("token", config.http.apikey)
        .asString
        .body
      
      val jsonPrice = parse(response) \\ "price"
      
      val returnPrice = (jsonPrice).extract[String]
      new Info(pair, BigDecimal(returnPrice.toDouble), Timestamp.now )
  }

  override def get(pair: Rate.Pair) : F[Error Either Rate] =
  {
    val inMemoBool = Memo.contains(pair)
    
    if (inMemoBool){
      val infoObject = Memo.get(pair)
      
      val time = infoObject.map(_.timeObj).get
      val price = infoObject.map(_.priceObj).get
      
      val offset = OffsetDateTime.now()
      val timeDif = Duration(offset.toEpochSecond - time.value.toEpochSecond, SECONDS)
    
      if (timeDif <= 300.second){
        Rate(pair, Price(price), time).asRight[Error].pure[F]
      }
      else{
        val infoToSave = requestFromAPI(pair)
        Memo += pair -> infoToSave
        Rate(pair, Price(BigDecimal(infoToSave.priceObj.toDouble)), Timestamp.now).asRight[Error].pure[F]
      }
    }
    else{
      val infoToSave = requestFromAPI(pair)
      Memo += pair -> infoToSave
      Rate(pair, Price(BigDecimal(infoToSave.priceObj.toDouble)), Timestamp.now).asRight[Error].pure[F]
    }
  }
}
