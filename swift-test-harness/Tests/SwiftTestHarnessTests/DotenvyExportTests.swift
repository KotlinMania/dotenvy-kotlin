#if canImport(Testing)
import Testing
import Dotenvy

@Suite("Dotenvy Swift Export Tests")
struct DotenvyExportTests {
    @Test("Dotenvy swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Dotenvy swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import Dotenvy

final class DotenvyExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Dotenvy swift module imported cleanly")
    }
}
#endif
