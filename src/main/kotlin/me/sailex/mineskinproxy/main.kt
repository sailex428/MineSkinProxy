package me.sailex.mineskinproxy

fun main(args : Array<String>) {
    val apiKey = System.getenv("MINE_SKIN_API_KEY")
    val port = System.getenv("API_PORT")?.toInt() ?: 8080
    val userAgent = System.getenv("USER_AGENT")?: "MineSkinProxy"
    if (apiKey != null) {
        MineSkinAPI(apiKey, userAgent).startListening(port)
    } else {
        println("MINE_SKIN_API_KEY not set")
    }
}