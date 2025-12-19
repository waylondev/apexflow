package dev.waylon.apexflow.core.comparison

import dev.waylon.apexflow.core.ApexFlow
import dev.waylon.apexflow.core.dsl.apexFlow
import dev.waylon.apexflow.core.dsl.execute
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
     * ApexFlow implementation of business flow - Component-based Best Practice
     *
     * Demonstrates core advantages of ApexFlow (compared to using Flow directly):
     * 1. Component-based design - Split complex flow into independent, reusable components
     * 2. Simple composition - Easily combine components using + operator
     * 3. Type safety - Complete compile-time type checking
     * 4. Declarative programming - Business flow is clear and readable
     * 5. Testability - Each component can be tested independently
     */
    private fun createApexFlow(): ApexFlow<Request, Response> {
        // 1. Validation Component - Independent, reusable, focused on data validation
        val validationComponent = apexFlow {
            map(::validatedRequest) // Use function reference for concise code
        }

        // 2. Parallel Processing Component - Independent, reusable, focused on parallel execution
        val parallelComponent = apexFlow {
            map { validated ->
                // Use coroutineScope to enable parallel processing
                coroutineScope {
                    // Execute database query and API call in parallel
                    val dbDeferred = async { queryDb(validated) } // Database query task
                    val apiDeferred = async { callThirdPartyApi(validated) } // API call task

                    // Wait for both tasks to complete and return results as a pair
                    Pair(dbDeferred.await(), apiDeferred.await())
                }
            }
        }

        // 3. Result Merge Component - Independent, reusable, focused on merging results
        val mergeComponent = apexFlow<Pair<DbResult, ApiResult>, MergedResult> {
            map { (dbResult, apiResult) ->
                // Merge database result and API result into a single MergedResult
                MergedResult(
                    id = dbResult.id,
                    dbData = dbResult.dbData,
                    apiData = apiResult.apiData
                )
            }
        }

        // 4. Response Build Component - Independent, reusable, focused on building response
        val responseComponent = apexFlow<MergedResult, Response> {
            map { merged ->
                // Build final response from merged result
                Response(
                    id = merged.id,
                    status = "SUCCESS",
                    data = merged
                )
            }
        }

        // 🌟 Core Advantage: Easily combine all components using + operator
        // Flow: Validation -> Parallel Processing -> Result Merge -> Response Build
        return validationComponent + parallelComponent + mergeComponent + responseComponent
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
            val response = apexFlow.execute(request).toList().first()
            assertEquals("SUCCESS", response.status)
        }

        val speedup = traditionalTime / apexFlowTime.toDouble()
        println(
            "Single Request Performance: Traditional=$traditionalTime ms, ApexFlow=$apexFlowTime ms, Speedup=$speedup x"
        )
    }


}

