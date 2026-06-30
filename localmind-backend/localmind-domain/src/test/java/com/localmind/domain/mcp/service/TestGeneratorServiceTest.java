package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.GeneratedTest;
import com.localmind.domain.mcp.port.out.TestGeneratorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestGeneratorServiceTest {

    @Mock
    private TestGeneratorRepository testGeneratorRepository;

    private TestGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new TestGeneratorService(testGeneratorRepository);
        lenient().when(testGeneratorRepository.save(any(GeneratedTest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ==================== generateUnitTests ====================

    @Test
    void generateUnitTests_javaCodeWith3Methods_finds3Functions() {
        String javaCode = """
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                    public int subtract(int a, int b) {
                        return a - b;
                    }
                    private double multiply(double a, double b) {
                        return a * b;
                    }
                }
                """;

        Map<String, Object> result = service.generateUnitTests(javaCode, "java", "junit");

        assertThat(result.get("language")).isEqualTo("java");
        assertThat(result.get("framework")).isEqualTo("junit");
        assertThat(result.get("functionsFound")).isEqualTo(3);
        assertThat(result.get("testsGenerated")).isEqualTo(9); // 3 funzioni x 3 test
        assertThat((String) result.get("testCode")).contains("add_shouldExistAndBeCallable");
        assertThat((String) result.get("testCode")).contains("subtract_shouldReturnExpectedResultWithValidInput");
        assertThat((String) result.get("testCode")).contains("multiply_shouldHandleEdgeCases");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> functions = (List<Map<String, Object>>) result.get("functions");
        assertThat(functions).hasSize(3);
        assertThat(functions.get(0).get("name")).isEqualTo("add");
        assertThat(functions.get(0).get("isExported")).isEqualTo(true);
        assertThat(functions.get(2).get("name")).isEqualTo("multiply");
        assertThat(functions.get(2).get("isExported")).isEqualTo(false); // private
    }

    @Test
    void generateUnitTests_typescriptWithArrowFunctions_findsAllFunctions() {
        String tsCode = """
                export async function fetchData(url: string) {
                    return await fetch(url);
                }
                export const processItems = (items: string[]) => {
                    return items.map(i => i.trim());
                }
                function helperUtil() {
                    return 42;
                }
                """;

        Map<String, Object> result = service.generateUnitTests(tsCode, "typescript", "vitest");

        assertThat(result.get("language")).isEqualTo("typescript");
        assertThat(result.get("framework")).isEqualTo("vitest");
        assertThat(result.get("functionsFound")).isEqualTo(3);
        assertThat(result.get("testsGenerated")).isEqualTo(9);
        assertThat((String) result.get("testCode")).contains("describe('fetchData'");
        assertThat((String) result.get("testCode")).contains("describe('processItems'");
        assertThat((String) result.get("testCode")).contains("describe('helperUtil'");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> functions = (List<Map<String, Object>>) result.get("functions");
        // fetchData: exported, async
        Map<String, Object> fetchFn = functions.stream()
                .filter(f -> "fetchData".equals(f.get("name")))
                .findFirst().orElseThrow();
        assertThat(fetchFn.get("isAsync")).isEqualTo(true);
        assertThat(fetchFn.get("isExported")).isEqualTo(true);

        // processItems: exported, not async (arrow with '(')
        Map<String, Object> processFn = functions.stream()
                .filter(f -> "processItems".equals(f.get("name")))
                .findFirst().orElseThrow();
        assertThat(processFn.get("isExported")).isEqualTo(true);

        // helperUtil: not exported
        Map<String, Object> helperFn = functions.stream()
                .filter(f -> "helperUtil".equals(f.get("name")))
                .findFirst().orElseThrow();
        assertThat(helperFn.get("isExported")).isEqualTo(false);
    }

    @Test
    void generateUnitTests_pythonWithDef_findsFunctions() {
        String pyCode = """
                def calculate_area(width, height):
                    return width * height

                def _private_helper():
                    pass

                def process_data(data):
                    return [d.strip() for d in data]
                """;

        Map<String, Object> result = service.generateUnitTests(pyCode, "python", "pytest");

        assertThat(result.get("language")).isEqualTo("python");
        assertThat(result.get("framework")).isEqualTo("pytest");
        assertThat(result.get("functionsFound")).isEqualTo(3);
        assertThat(result.get("testsGenerated")).isEqualTo(9);
        assertThat((String) result.get("testCode")).contains("class TestCalculate_area:");
        assertThat((String) result.get("testCode")).contains("def test_should_exist_and_be_callable");
        assertThat((String) result.get("testCode")).contains("import pytest");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> functions = (List<Map<String, Object>>) result.get("functions");
        // _private_helper: starts with _, not exported
        Map<String, Object> privateFn = functions.stream()
                .filter(f -> "_private_helper".equals(f.get("name")))
                .findFirst().orElseThrow();
        assertThat(privateFn.get("isExported")).isEqualTo(false);
    }

    @Test
    void generateUnitTests_defaultLanguageIsJava() {
        String code = "public void doSomething() {}";

        Map<String, Object> result = service.generateUnitTests(code, null, null);

        assertThat(result.get("language")).isEqualTo("java");
        assertThat(result.get("framework")).isEqualTo("junit");
    }

    @Test
    void generateUnitTests_defaultFrameworkBasedOnLanguage() {
        assertThat(service.resolveFramework(null, "java")).isEqualTo("junit");
        assertThat(service.resolveFramework(null, "typescript")).isEqualTo("vitest");
        assertThat(service.resolveFramework(null, "javascript")).isEqualTo("vitest");
        assertThat(service.resolveFramework(null, "python")).isEqualTo("pytest");
    }

    @Test
    void generateUnitTests_savesToRepository() {
        String code = "public void hello() {}";

        service.generateUnitTests(code, "java", "junit");

        ArgumentCaptor<GeneratedTest> captor = ArgumentCaptor.forClass(GeneratedTest.class);
        verify(testGeneratorRepository).save(captor.capture());

        GeneratedTest saved = captor.getValue();
        assertThat(saved.getId()).isNotNull().isNotBlank();
        assertThat(saved.getLanguage()).isEqualTo("java");
        assertThat(saved.getFramework()).isEqualTo("junit");
        assertThat(saved.getFunctionsFound()).isEqualTo(1);
        assertThat(saved.getTestCode()).isNotBlank();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void generateUnitTests_emptyCode_returnsZeroFunctions() {
        Map<String, Object> result = service.generateUnitTests("", "java", "junit");

        assertThat(result.get("functionsFound")).isEqualTo(0);
        assertThat(result.get("testsGenerated")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> functions = (List<Map<String, Object>>) result.get("functions");
        assertThat(functions).isEmpty();
    }

    @Test
    void generateUnitTests_javaExcludesKeywords() {
        String code = """
                public class Example {
                    public void process() {
                        if (true) {}
                        for (int i = 0; i < 10; i++) {}
                        while (true) {}
                        switch (x) {}
                    }
                }
                """;

        Map<String, Object> result = service.generateUnitTests(code, "java", "junit");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> functions = (List<Map<String, Object>>) result.get("functions");
        List<String> names = functions.stream()
                .map(f -> (String) f.get("name"))
                .toList();
        assertThat(names).contains("process");
        assertThat(names).doesNotContain("if", "for", "while", "switch");
    }

    // ==================== findEdgeCases ====================

    @Test
    void findEdgeCases_codeWithStringOperations_detectsEdgeCases() {
        String code = """
                public String process(String input) {
                    return input.trim().split(",")[0];
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        assertThat((int) result.get("totalEdgeCases")).isGreaterThan(0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> categories = edgeCases.stream()
                .map(ec -> (String) ec.get("category"))
                .toList();
        assertThat(categories).contains("string");

        @SuppressWarnings("unchecked")
        Map<String, Integer> byCategory = (Map<String, Integer>) result.get("byCategory");
        assertThat(byCategory).containsKey("string");
    }

    @Test
    void findEdgeCases_codeWithNumericAndDivision_detectsMultipleCategories() {
        String code = """
                public double calculate(int numerator, int denominator) {
                    int result = numerator / denominator;
                    return result + Integer.parseInt("42");
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> descriptions = edgeCases.stream()
                .map(ec -> (String) ec.get("description"))
                .toList();
        assertThat(descriptions).contains("Division by zero");
        assertThat(descriptions).contains("Zero value");
        assertThat(descriptions).contains("Negative value");

        @SuppressWarnings("unchecked")
        Map<String, Integer> bySeverity = (Map<String, Integer>) result.get("bySeverity");
        assertThat(bySeverity.get("high")).isGreaterThan(0);
    }

    @Test
    void findEdgeCases_codeWithAsync_detectsAsyncEdgeCases() {
        String code = """
                public CompletableFuture<String> fetchData() {
                    return CompletableFuture.supplyAsync(() -> "data");
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> categories = edgeCases.stream()
                .map(ec -> (String) ec.get("category"))
                .toList();
        assertThat(categories).contains("async");
    }

    @Test
    void findEdgeCases_codeWithFileIO_detectsFileEdgeCases() {
        String code = """
                public String readFile(String path) throws IOException {
                    BufferedReader reader = new BufferedReader(new FileReader(path));
                    return Files.readString(Path.of(path));
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> descriptions = edgeCases.stream()
                .map(ec -> (String) ec.get("description"))
                .toList();
        assertThat(descriptions).contains("File not found");
        assertThat(descriptions).contains("Permission denied");
    }

    @Test
    void findEdgeCases_noEdgeCases_returnsEmptyResult() {
        String code = "int x = 42;";

        Map<String, Object> result = service.findEdgeCases(code);

        assertThat((int) result.get("totalEdgeCases")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        assertThat(edgeCases).isEmpty();

        @SuppressWarnings("unchecked")
        Map<String, Integer> bySeverity = (Map<String, Integer>) result.get("bySeverity");
        assertThat(bySeverity.get("high")).isEqualTo(0);
        assertThat(bySeverity.get("medium")).isEqualTo(0);
        assertThat(bySeverity.get("low")).isEqualTo(0);
    }

    @Test
    void findEdgeCases_savesToRepository() {
        String code = "input.trim()";

        service.findEdgeCases(code);

        ArgumentCaptor<GeneratedTest> captor = ArgumentCaptor.forClass(GeneratedTest.class);
        verify(testGeneratorRepository).save(captor.capture());
        GeneratedTest saved = captor.getValue();
        assertThat(saved.getFramework()).isEqualTo("edge-case-analysis");
    }

    @Test
    void findEdgeCases_codeWithTryCatch_detectsErrorHandling() {
        String code = """
                try {
                    riskyOperation();
                } catch (Exception e) {
                    log.error("Error", e);
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> categories = edgeCases.stream()
                .map(ec -> (String) ec.get("category"))
                .toList();
        assertThat(categories).contains("error-handling");
    }

    @Test
    void findEdgeCases_codeWithNullChecks_detectsNullSafety() {
        String code = """
                Optional<String> value = Optional.ofNullable(input);
                if (obj != null) {
                    obj.doSomething();
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> categories = edgeCases.stream()
                .map(ec -> (String) ec.get("category"))
                .toList();
        assertThat(categories).contains("null-safety");
    }

    @Test
    void findEdgeCases_codeWithRegex_detectsRegexEdgeCases() {
        String code = """
                Pattern pattern = Pattern.compile("[a-z]+");
                String result = input.replaceAll("[^a-z]", "");
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> categories = edgeCases.stream()
                .map(ec -> (String) ec.get("category"))
                .toList();
        assertThat(categories).contains("regex");
    }

    // ==================== analyzeCoverage ====================

    @Test
    void analyzeCoverage_fullCoverage_returns100Percent() {
        String sourceCode = """
                public void add() {}
                public void remove() {}
                """;

        String testCode = """
                @Test
                void add_works() {
                    calculator.add(1, 2);
                }
                @Test
                void remove_works() {
                    calculator.remove(1);
                }
                """;

        Map<String, Object> result = service.analyzeCoverage(sourceCode, testCode);

        assertThat(result.get("totalFunctions")).isEqualTo(2);
        assertThat(result.get("coveragePercentage")).isEqualTo(100);

        @SuppressWarnings("unchecked")
        List<String> covered = (List<String>) result.get("coveredFunctions");
        assertThat(covered).containsExactlyInAnyOrder("add", "remove");

        @SuppressWarnings("unchecked")
        List<String> uncovered = (List<String>) result.get("uncoveredFunctions");
        assertThat(uncovered).isEmpty();
    }

    @Test
    void analyzeCoverage_zeroCoverage_returns0Percent() {
        String sourceCode = """
                public void processOrder() {}
                public void sendEmail() {}
                """;

        String testCode = """
                @Test
                void someUnrelatedTest() {
                    assertThat(true).isTrue();
                }
                """;

        Map<String, Object> result = service.analyzeCoverage(sourceCode, testCode);

        assertThat(result.get("totalFunctions")).isEqualTo(2);
        assertThat(result.get("coveragePercentage")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        List<String> uncovered = (List<String>) result.get("uncoveredFunctions");
        assertThat(uncovered).containsExactlyInAnyOrder("processOrder", "sendEmail");
    }

    @Test
    void analyzeCoverage_partialCoverage_returnsCorrectPercentage() {
        String sourceCode = """
                public void save() {}
                public void delete() {}
                public void update() {}
                public void find() {}
                """;

        String testCode = """
                describe('save', () => {
                    it('should save data', () => {
                        save();
                    });
                });
                test('find works', () => {
                    find();
                });
                """;

        Map<String, Object> result = service.analyzeCoverage(sourceCode, testCode);

        assertThat(result.get("totalFunctions")).isEqualTo(4);
        assertThat(result.get("coveragePercentage")).isEqualTo(50);

        @SuppressWarnings("unchecked")
        List<String> covered = (List<String>) result.get("coveredFunctions");
        assertThat(covered).contains("save", "find");

        @SuppressWarnings("unchecked")
        List<String> uncovered = (List<String>) result.get("uncoveredFunctions");
        assertThat(uncovered).contains("delete", "update");
    }

    @Test
    void analyzeCoverage_noFunctionsInSourceCode_returnsZeroTotal() {
        String sourceCode = "int x = 42;";
        String testCode = "@Test void test() {}";

        Map<String, Object> result = service.analyzeCoverage(sourceCode, testCode);

        assertThat(result.get("totalFunctions")).isEqualTo(0);
        assertThat(result.get("coveragePercentage")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) result.get("details");
        assertThat(details).isEmpty();
    }

    @Test
    void analyzeCoverage_detailsContainTestMatches() {
        String sourceCode = "public void calculate() {}";
        String testCode = """
                describe('calculate', () => {
                    it('should calculate correctly', () => {
                        calculate(1, 2);
                    });
                });
                """;

        Map<String, Object> result = service.analyzeCoverage(sourceCode, testCode);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) result.get("details");
        assertThat(details).hasSize(1);

        Map<String, Object> detail = details.get(0);
        assertThat(detail.get("functionName")).isEqualTo("calculate");
        assertThat(detail.get("hasCoverage")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        List<String> testMatches = (List<String>) detail.get("testMatches");
        assertThat(testMatches).contains("describe/class block", "direct call");
    }

    @Test
    void analyzeCoverage_savesToRepository() {
        String sourceCode = "public void doWork() {}";
        String testCode = "doWork()";

        service.analyzeCoverage(sourceCode, testCode);

        ArgumentCaptor<GeneratedTest> captor = ArgumentCaptor.forClass(GeneratedTest.class);
        verify(testGeneratorRepository).save(captor.capture());
        GeneratedTest saved = captor.getValue();
        assertThat(saved.getFramework()).isEqualTo("coverage-analysis");
        assertThat(saved.getFunctionsFound()).isEqualTo(1);
    }

    // ==================== Helper method tests ====================

    @Test
    void resolveLanguage_blankDefaultsToJava() {
        assertThat(service.resolveLanguage(null)).isEqualTo("java");
        assertThat(service.resolveLanguage("")).isEqualTo("java");
        assertThat(service.resolveLanguage("  ")).isEqualTo("java");
    }

    @Test
    void resolveLanguage_normalizesToLowerCase() {
        assertThat(service.resolveLanguage("JAVA")).isEqualTo("java");
        assertThat(service.resolveLanguage("TypeScript")).isEqualTo("typescript");
        assertThat(service.resolveLanguage(" Python ")).isEqualTo("python");
    }

    @Test
    void extractAllFunctionNames_multiLanguageDetection() {
        String mixedCode = """
                public void javaMethod() {}
                export function tsFunction() {}
                export const arrowFn = (x) => x;
                def python_func():
                    pass
                """;

        List<String> names = service.extractAllFunctionNames(mixedCode);

        assertThat(names).contains("javaMethod", "tsFunction", "arrowFn", "python_func");
    }

    @Test
    void findTestMatches_detectsDescribeBlock() {
        String testCode = "describe('myFunc', () => {});";
        List<String> matches = service.findTestMatches("myFunc", testCode);
        assertThat(matches).contains("describe/class block");
    }

    @Test
    void findTestMatches_detectsDirectCall() {
        String testCode = "myFunc(1, 2, 3);";
        List<String> matches = service.findTestMatches("myFunc", testCode);
        assertThat(matches).contains("direct call");
    }

    @Test
    void findTestMatches_noMatch_returnsEmpty() {
        String testCode = "someOtherFunction();";
        List<String> matches = service.findTestMatches("myFunc", testCode);
        assertThat(matches).isEmpty();
    }

    @Test
    void findEdgeCases_codeWithCollectionOps_detectsCollectionEdgeCases() {
        String code = """
                public void process(List<String> items) {
                    items.stream().filter(i -> !i.isEmpty()).map(String::toUpperCase);
                    String first = items.get(0);
                }
                """;

        Map<String, Object> result = service.findEdgeCases(code);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> edgeCases = (List<Map<String, Object>>) result.get("edgeCases");
        List<String> categories = edgeCases.stream()
                .map(ec -> (String) ec.get("category"))
                .toList();
        assertThat(categories).contains("collection");

        @SuppressWarnings("unchecked")
        Map<String, Integer> byCategory = (Map<String, Integer>) result.get("byCategory");
        assertThat(byCategory.get("collection")).isGreaterThanOrEqualTo(3); // empty, single, large
    }
}
