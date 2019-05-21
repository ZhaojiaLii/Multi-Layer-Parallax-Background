package com.example.multi_layerparallax.Models

import com.google.gson.annotations.SerializedName
object Model {
    data class Result(val query: Query)
    data class Query(val searchinfo: SearchInfo)
    data class SearchInfo(val totalhits: Int)
}

