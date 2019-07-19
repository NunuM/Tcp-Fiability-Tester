package util

import java.time

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try


/**
  * Created by nuno on 27-04-2017.
  */
object AppConfig {


  lazy val conf: Config = ConfigFactory.load

  val node: String = Try(conf.getString("node")).getOrElse("localhost")

  val port: Int = Try(conf.getInt("port")).getOrElse(80)

  val testDuration: time.Duration = Try(conf.getDuration("dur"))
    .getOrElse(java.time.Duration.ofSeconds(3600l))

  val testInterval: time.Duration = Try(conf.getDuration("inter"))
    .getOrElse(java.time.Duration.ofSeconds(3600l))

  val testConnectionTimeout: time.Duration = Try(conf.getDuration("timeout"))
    .getOrElse(java.time.Duration.ofSeconds(3600l))

  val appName: String = Try(conf.getString("app.name")).getOrElse("TCP")

}
