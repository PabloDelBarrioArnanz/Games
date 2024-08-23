package org.delbarriopablo

import org.delbarriopablo.Game.BACKGROUND_IMAGE
import org.delbarriopablo.Game.BIRD_IMAGE
import org.delbarriopablo.Game.BIRD_JUMP
import org.delbarriopablo.Game.BIRD_UP_VELOCITY
import org.delbarriopablo.Game.BIRTH_HEIGHT
import org.delbarriopablo.Game.BIRTH_INIT_X
import org.delbarriopablo.Game.BIRTH_INIT_Y
import org.delbarriopablo.Game.BIRTH_WIDTH
import org.delbarriopablo.Game.BOARD_HEIGHT
import org.delbarriopablo.Game.BOARD_WIDTH
import org.delbarriopablo.Game.BOTTOM_PIPE_IMAGE
import org.delbarriopablo.Game.GAME_OVER
import org.delbarriopablo.Game.GRAVITY
import org.delbarriopablo.Game.PIPES_LEFT_VELOCITY
import org.delbarriopablo.Game.PIPE_FREE_SPACE
import org.delbarriopablo.Game.PIPE_HEIGHT
import org.delbarriopablo.Game.PIPE_WIDTH
import org.delbarriopablo.Game.SCORE
import org.delbarriopablo.Game.TOP_PIPE_IMAGE
import org.delbarriopablo.Game.increaseScore
import org.delbarriopablo.Game.restart
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*


object Game {
    const val BOARD_WIDTH = 360
    const val BOARD_HEIGHT = 640

    const val PIPES_LEFT_VELOCITY = -4
    var BIRD_UP_VELOCITY = 0
    const val BIRD_JUMP = -90

    const val GRAVITY = 3

    const val BIRTH_INIT_X = BOARD_WIDTH / 8
    const val BIRTH_INIT_Y = BOARD_HEIGHT / 2
    const val BIRTH_WIDTH = 34
    const val BIRTH_HEIGHT = 24

    const val PIPE_WIDTH = 64
    const val PIPE_HEIGHT = 512
    const val PIPE_FREE_SPACE = BOARD_HEIGHT / 4

    val BACKGROUND_IMAGE: Image = ImageIcon(readResource("./flappybirdbg.png")).image
    val BIRD_IMAGE: Image = ImageIcon(readResource("./flappybird.png")).image
    val TOP_PIPE_IMAGE: Image = ImageIcon(readResource("./toppipe.png")).image
    val BOTTOM_PIPE_IMAGE: Image = ImageIcon(readResource("./bottompipe.png")).image

    var SCORE = 0.0
    var GAME_OVER = false

    fun increaseScore() {
        SCORE += 0.5
    }

    fun restart() {
        GAME_OVER = false
        BIRD_UP_VELOCITY = 0
        SCORE = 0.0
    }

    private fun readResource(resourcePath: String): ByteArray {
        return javaClass.classLoader.getResourceAsStream(resourcePath)?.readAllBytes()!!
    }
}

fun main() {

    val mainFrame = JFrame("Flappy Bird")
        .apply {
            isResizable = false
            setLocationRelativeTo(null)
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            setSize(BOARD_WIDTH, BOARD_HEIGHT)
        }

    mainFrame.add(FlappyBird().apply {
        requestFocus()
    })
    mainFrame.pack()
    mainFrame.isVisible = true
}

class FlappyBird : JPanel(), ActionListener, KeyListener {

    init {
        preferredSize = Dimension(BOARD_WIDTH, BOARD_HEIGHT)
        background = Color.BLUE
        isFocusable = true
        addKeyListener(this)
    }


    private val bird = Bird()
    private val pipes: MutableList<Pipe> = mutableListOf()

    private val gameLoop = Timer(1000 / 60, this).apply { start() }
    private val pipesLoop = Timer(1500) {
        placePipes()
    }.apply { start() }

    override fun paintComponent(graphics: Graphics?) {
        super.paintComponent(graphics)
        graphics?.drawImage(BACKGROUND_IMAGE, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null)
        graphics?.drawImage(BIRD_IMAGE, bird.x, bird.y, BIRTH_WIDTH, BIRTH_HEIGHT, null)
        pipes.forEach { pipe ->
            graphics?.drawImage(pipe.getImage(), pipe.x, pipe.y, PIPE_WIDTH, PIPE_HEIGHT, null)
        }
        graphics?.color = Color.WHITE
        graphics?.font = Font("Arial", Font.BOLD, 32)

        if (GAME_OVER) graphics?.drawString("Game over: ${SCORE.toInt()}", 100, 300)
        else graphics?.drawString("${SCORE.toInt()}", 10, 35)
    }

    private fun move() {
        bird.y = (bird.y + BIRD_UP_VELOCITY + GRAVITY).coerceAtLeast(0)
        BIRD_UP_VELOCITY = 0
        pipes.forEach { pipe -> pipe.x += PIPES_LEFT_VELOCITY }

        if (bird.y > BOARD_HEIGHT) GAME_OVER = true
        pipes.forEach { pipe ->

            if (!pipe.passed && bird.x > pipe.x + PIPE_WIDTH) {
                pipe.passed = true
                increaseScore()
            }

            if (bird.x < pipe.x + PIPE_WIDTH &&
                bird.x + BIRTH_WIDTH > pipe.x &&
                bird.y < pipe.y + PIPE_HEIGHT &&
                bird.y + BIRTH_HEIGHT > pipe.y
            ) GAME_OVER = true
        }
    }

    private fun placePipes() {
        val randomPipeY = (0 - PIPE_HEIGHT / 4 - (Math.random() * PIPE_HEIGHT / 2)).toInt()
        pipes.add(TopPipe(y = randomPipeY))
        pipes.add(BottomPipe(y = randomPipeY + PIPE_HEIGHT + PIPE_FREE_SPACE))
    }

    override fun actionPerformed(e: ActionEvent?) {
        move()
        repaint()
        if (GAME_OVER) {
            pipesLoop.stop()
            gameLoop.stop()
        }
    }

    override fun keyTyped(e: KeyEvent?) {
        BIRD_UP_VELOCITY = BIRD_JUMP
        if (GAME_OVER) {
            restart()
            pipes.clear()
            gameLoop.start()
            pipesLoop.start()
            bird.x = BIRTH_INIT_X
            bird.y = BIRTH_INIT_Y
        }
    }

    override fun keyPressed(e: KeyEvent?) {}

    override fun keyReleased(e: KeyEvent?) {}

}

class Bird(
    var x: Int = BIRTH_INIT_X,
    var y: Int = BIRTH_INIT_Y
)

open class Pipe(
    var x: Int = BOARD_WIDTH,
    var y: Int = 0,
    var passed: Boolean = false
) {
    open fun getImage() = TOP_PIPE_IMAGE
}

class TopPipe(
    x: Int = BOARD_WIDTH,
    y: Int = 0,
    passed: Boolean = false
) : Pipe(x, y, passed)

class BottomPipe(
    x: Int = BOARD_WIDTH,
    y: Int = 0,
    passed: Boolean = false
) : Pipe(x, y, passed) {
    override fun getImage() = BOTTOM_PIPE_IMAGE
}

