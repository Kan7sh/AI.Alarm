package com.example.aialarm.utlis


class RepeatConverter {

    companion object{
        fun fromList(value: List<Int>): String {
            return value.joinToString(",")
        }

        fun toList(value: String): List<Int> {
            return value.split(",").map { it.toInt() }
        }
    }


}
