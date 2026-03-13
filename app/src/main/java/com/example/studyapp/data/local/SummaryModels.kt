package com.example.studyapp.data.local

import androidx.annotation.Keep

@Keep
data class SummaryResponse(
    val sections: List<SummarySection>
)

@Keep
data class SummarySection(
    val title: String,
    var content: String
)
