/* Readme
This class is not for storing into Database,
but for making a JSON like HTTP Req by using FUEL
 */
package main.kotlin.pojo.httpReq

import lombok.Getter
import lombok.Setter
@Getter
@Setter
data class FuelHttpReq(
        val header: Map<String, String?>,
        val body: Any?
)

/* Tutorial for JACKSON
    ~~~If we are going to sending out this HTTP Post~~~
    {
        "header": {
            "Content-Type": "application/json"
        },
        "body": {
            "param": {
                "vessel_ename": "HONG TAI 26",
                "voyage": "C0525",
                "bill_no": "HT260525SKR",
                "ie_flag": "O",
                "cntr_no": "FSCU0310556"
            },
            "signature": "SONGX"
        }
    }

    ~~~We need a POJO first~~~
    DPLRegHttpReq
        val param: Map<String, String?>,
        val signature: String

    ~~~Map is for {}, while List is for []~~~

    ~~~And then we create a Map~~~
    val paramMap : Map<String, String?> = mapOf(
                "vessel_ename" to "XIN HAI MING",
                "voyage" to "1904AS",
                "bill_no" to "HT260525SKR",
                "ie_flag" to "O",
                "cntr_no" to "XHM1904SDLSH009"
        )

    ~~~Finally we put everything together~~~
    val dPLRegHttpReqBody = DPLRegHttpReq (paramMap,"SONGX")
    val httpReqBody:FuelHttpReq? = FuelHttpReq(mapOf("Content-Type" to "application/json"), dPLRegHttpReqBody)

    ~~~You can output to verify what you have finally make~~~
    logger.debug {  objectMapper.writeValueAsString(httpReqBody)}
 */