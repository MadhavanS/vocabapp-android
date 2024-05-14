package com.mad.vocab.data.models

data class Pagination(
    val limit: Int,
    val next: Int,
    val prev: Int,
    val total: Int
)