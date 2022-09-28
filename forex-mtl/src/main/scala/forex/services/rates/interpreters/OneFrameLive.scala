package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.errors._
import scala.concurrent.duration._
import forex.config._

import java.time.OffsetDateTime

import scalaj.http.{Http, HttpOptions}

class OneFrameLive[F[_]: Applicative] extends Algebra[F] {
  
  val Memo = new scala.collection.mutable.HashMap[Rate.Pair, (OffsetDateTime, BigDecimal)]

  override def get(pair: Rate.Pair): F[Error Either Rate] =
  {
    //Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]
    
    if (Memo.contains(pair)){
      val (time, price) = Memo.get(pair)
      
      private val offset = OffsetDateTime.now().getOffset
      val timeDif = Duration(offset.getMillis - time.getMillis, MILLISECONDS)
      
      val longDif = timeDif.getSeconds()
      
      if (longDif <= 300){
        price
      }
      else{
        config <- Config.stream("app")
        
        val response = Http("localhost:8080/rates")
        .param("pair", pair.from + pair.to)
        .headers(Seq("Authorization" -> ("token: " + config.http.api_key), "Accept" -> "application/json"))
        .asString
        .body
        
        retPrice = response.get("price")
      }
    }
  }
}
