package com.robbiebowman.test

import com.robbiebowman.jsonschemaannotation.JsonSchema

@JsonSchema("Testy test")
class MyTestClass {

    fun doSomething() {
        MyTestClassBuilder().build()
    }
}