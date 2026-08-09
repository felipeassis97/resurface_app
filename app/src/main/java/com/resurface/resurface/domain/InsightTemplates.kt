package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.Tone

/**
 * Frase local (fallback e primeira exibição) do tip, no tom. Em inglês, sem travessões, wording
 * honesto (pico é por hora de INÍCIO do episódio, então "start", não "watch"). Puro.
 */
class InsightTemplates {

    fun phrase(insight: Insight, tone: Tone): Message = when (insight.type) {
        InsightType.PEAK_HOUR -> {
            val h = insight.value
            val w = "${h}h and ${(h + 1) % 24}h"
            tone.pick(
                Message("You start most between $w.", "That is your busiest window."),
                Message("You tend to open between $w.", "Nice to know your own rhythm."),
                Message("Your $w slot runs hot.", "The feed clocked in early too."),
            )
        }
        InsightType.PEAK_DAY -> tone.pick(
            Message("${insight.label} is your heaviest day.", "Worth a glance."),
            Message("${insight.label} tends to be the fullest.", "Just something to notice."),
            Message("${insight.label} takes the crown.", "Every week has a champion."),
        )
        InsightType.TREND -> {
            val down = insight.value <= 0
            val pct = kotlin.math.abs(insight.value)
            if (down) tone.pick(
                Message("Short video is down $pct% this week.", "Compared to last week."),
                Message("A little lighter this week, down $pct%.", "However that feels to you."),
                Message("Down $pct% versus last week.", "The scroll took a breather."),
            ) else tone.pick(
                Message("Short video is up $pct% this week.", "Compared to last week."),
                Message("A bit more this week, up $pct%.", "Just so you know."),
                Message("Up $pct% versus last week.", "The feed had a good run."),
            )
        }
        InsightType.CROSS_APP -> tone.pick(
            Message("${insight.value} sessions spanned both apps.", "Instagram into TikTok and back."),
            Message("${insight.value} sessions crossed both apps.", "The count adds up across them."),
            Message("${insight.value} app hops this week.", "One feed is never enough, apparently."),
        )
        InsightType.VIDEOS -> tone.pick(
            Message("${insight.value} videos this week.", "That is the running tally."),
            Message("${insight.value} videos so far this week.", "Just a number, no verdict."),
            Message("${insight.value} videos and counting.", "Your thumb has been busy."),
        )
        InsightType.WELCOME -> tone.pick(
            Message("Welcome to Resurface.", "Your weekly patterns will show up here."),
            Message("Hi there.", "Patterns will appear here as you go."),
            Message("Here we go.", "The interesting bits land here soon."),
        )
    }
}

/** Escolhe a variante pelo tom. */
private fun Tone.pick(direct: Message, gentle: Message, playful: Message): Message = when (this) {
    Tone.DIRETO -> direct
    Tone.GENTIL -> gentle
    Tone.BEM_HUMORADO -> playful
}
