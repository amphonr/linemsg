package com.modong.linemsg.linemsg

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@LineMessageHandler
class LinemsgApplication

fun main(args: Array<String>) {
	runApplication<LinemsgApplication>(*args)
}

@EventMapping
fun handleTextMessage(e: MessageEvent<TextMessageContent?>): Message? {
	println("event: $e")
	val message: TextMessageContent? = e.message
	return TextMessage(message?.text)
}