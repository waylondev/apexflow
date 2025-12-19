package dev.waylon.apexflow.core.comparison

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Business flow comparison test using ApexFlow
 *
 * Business flow:
 * 1. Data validation
 * 2. Query database
 * 3. Request third-party API
 * 4. Merge results from 2 and 3
 * 5. Organize response
 */
class BusinessFlowComparisonTest {

    private val logger = LoggerFactory.getLogger(BusinessFlowComparisonTest::class.java)

    // Test data classes
    data class Request(val id: String, val name: String)
    data class ValidatedRequest(val id: String, val name: String)
    data class DbResult(val id: String, val dbData: String)
    data class ApiResult(val id: String, val apiData: String)
    data class MergedResult(val id: String, val dbData: String, val apiData: String)
    data class Response(val id: String, val status: String, val data: MergedResult)

    // Simulated DB query delay
    private val dbDelay = 1000L

    // Simulated API request delay
    private val apiDelay = 1500L

    private fun validatedRequest(request: Request): ValidatedRequest {
        if (request.id.isBlank() || request.name.isBlank()) {
            throw IllegalArgumentException("Invalid request: id and name must not be blank")
        }
        val validatedRequest = ValidatedRequest(request.id, request.name)
        return validatedRequest
    }

    // Simulate database query
    private suspend fun queryDb(request: ValidatedRequest): DbResult {
        delay(dbDelay) // Simulate database query delay
        return DbResult(request.id, "db_data_${request.id}")
    }

    // Simulate third-party API call
    private suspend fun callThirdPartyApi(request: ValidatedRequest): ApiResult {
        delay(apiDelay) // Simulate API request delay
        return ApiResult(request.id, "api_data_${request.id}")
    }

    /**
     * Traditional implementation of business flow
     */
    private suspend fun traditionalBusinessFlow(request: Request): Response {
        // 1. Data validation
        val validatedRequest = validatedRequest(request)

        // 2. Query database
        val dbResult = queryDb(validatedRequest)

        // 3. Request third-party API
        val apiResult = callThirdPartyApi(validatedRequest)

        // 4. Merge results
        val mergedResult = MergedResult(
            id = validatedRequest.id,
            dbData = dbResult.dbData,
            apiData = apiResult.apiData
        )

        // 5. Organize response
        return Response(
            id = validatedRequest.id,
            status = "SUCCESS",
            data = mergedResult
        )
    }

    /**
     * ApexFlow implementation of business flow
     */
    private fun createApexFlow(): ApexFlow<Request, Response> {
        val validateFlow = apexFlow {
            map(::validatedRequest)
        }


        val parallelAndMergeFlow = apexFlow {
            map { validatedRequest ->
                val dbResult = queryDb(validatedRequest)
                val apiResult = callThirdPartyApi(validatedRequest)

                MergedResult(
                    id = validatedRequest.id,
                    dbData = dbResult.dbData,
                    apiData = apiResult.apiData
                )
            }
        }

        val responseFlow = apexFlow<MergedResult, Response> {
            map { mergedResult ->
                Response(
                    id = mergedResult.id,
                    status = "SUCCESS",
                    data = mergedResult
                )
            }
        }

        return validateFlow + parallelAndMergeFlow + responseFlow
    }


    /**
     * Single request performance comparison
     */
    @Test
    fun `compare single request performance`() = runBlocking {
        val request = Request("123", "test")

        // Test traditional implementation
        val traditionalTime = measureTimeMillis {
            val response = traditionalBusinessFlow(request)
            assertEquals("SUCCESS", response.status)
        }

        // Test ApexFlow implementation
        val apexFlow = createApexFlow()
        val apexFlowTime = measureTimeMillis {
            val response = apexFlow.execute(request).first()
            assertEquals("SUCCESS", response.status)
        }

        logger.info(
            "Single Request Performance: Traditional={}ms, ApexFlow={}ms, Speedup={}x",
            traditionalTime, apexFlowTime, traditionalTime / apexFlowTime.toDouble()
        )
    }


    /**
     * Demonstrate ApexFlow advantages
     */
    @Test
    fun `demo apexflow advantages`() {
        logger.info("ApexFlow advantages demo: Declarative programming, asynchronous processing, composition capability, type safety, plugin mechanism, reactive design")
    }
}

