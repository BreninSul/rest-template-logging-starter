package io.github.breninsul.rest.logging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [TestRequestConfig::class])
open class TestRequest(
) {
    @Autowired
    var service: TestService?=null

    @Test
    open fun test() {
        val response =service!!.test()
        val img=response.use { it.readAllBytes() }
        println(img.size)
    }


}