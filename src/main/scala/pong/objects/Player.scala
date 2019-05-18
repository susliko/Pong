package pong.objects

import monocle.macros.Lenses

@Lenses
case class Player(score: Int = 0, paddle: Paddle)
