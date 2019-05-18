package pong.game

import pong.objects.{AIPaddle, HumanPaddle}

import scala.concurrent.duration.FiniteDuration

case class GameSettings(tpf: Double,
                        sensitivity: Double,
                        highScore: Int,
                        vxRange: (Double, Double),
                        vyRange: (Double, Double),
                        multiplayerPort: Int,
                        roundSleepTime: Int,
                        rightHumanPaddle: HumanPaddle,
                        rightAIPaddle: AIPaddle,
                        clientWaitingTimeout: FiniteDuration)
