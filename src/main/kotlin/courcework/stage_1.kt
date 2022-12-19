package courcework

import java.io.File

fun main() {

    val wordsTxt = File("words.txt")

    wordsTxt.readLines().forEach { println(it) }

}