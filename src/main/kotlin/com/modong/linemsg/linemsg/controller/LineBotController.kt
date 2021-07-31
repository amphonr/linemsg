package com.modong.linemsg.linemsg.controller

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.response.BotApiResponse
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import lombok.NonNull
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.logging.Logger

@Slf4j
@LineMessageHandler
class LineBotController {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = Logger.getLogger(javaClass.enclosingClass.toString())
    }

    fun log(s: String) {
        logger.info(s)
    }

    @Autowired
    private val lineMessagingClient: LineMessagingClient? = null

    @EventMapping
    fun handleTextMessage(event: MessageEvent<TextMessageContent?>) {
        log(event.toString())
        val message: TextMessageContent? = event.getMessage()
        if (message != null) {
            handleTextContent(event.getReplyToken(), event, message)
        }
    }

    open fun handleTextContent(
        replyToken: String, event: Event,
        content: TextMessageContent
    ) {
        val text: String = content.getText()
        log("Got text message from %s : %s"+replyToken+text)
        when (text) {
            "Profile" -> {
                val userId: String = event.getSource().getUserId()
                if (userId != null) {
                    lineMessagingClient?.getProfile(userId)
                        ?.whenComplete { profile, throwable ->
                            if (throwable != null) {
                                throwable.message?.let { replyText(replyToken, it) }
                                return@whenComplete
                            }
                            this.reply(
                                replyToken, Arrays.asList(
                                    TextMessage(
                                        "Display name: " +
                                                profile.getDisplayName()
                                    ),
                                    TextMessage(
                                        "Status message: " +
                                                profile.getStatusMessage()
                                    ),
                                    TextMessage(
                                        "User ID: " +
                                                profile.getUserId()
                                    )
                                )
                            )
                        }
                }
            }
            else -> {
                log("Return echo message %s : %s"+ replyToken + text)
                replyText(replyToken, text)
            }
        }
    }

    open fun replyText(
        @NonNull replyToken: String,
        @NonNull message: String
    ) {
        var message = message
        require(!replyToken.isEmpty()) { "replyToken is not empty" }
        if (message.length > 1000) {
            message = message.substring(0, 1000 - 2) + "..."
        }
        this.reply(replyToken, TextMessage(message))
    }

    open fun reply(@NonNull replyToken: String, @NonNull message: Message) {
        reply(replyToken, listOf(message))
    }

    open fun reply(@NonNull replyToken: String, @NonNull messages: List<Message>) = try {
        val response: BotApiResponse? = lineMessagingClient?.replyMessage(
            ReplyMessage(replyToken, messages)
        )?.get()
    } catch (e: InterruptedException) {
        throw RuntimeException(e)
    } catch (e: ExecutionException) {
        throw RuntimeException(e)
    }
}