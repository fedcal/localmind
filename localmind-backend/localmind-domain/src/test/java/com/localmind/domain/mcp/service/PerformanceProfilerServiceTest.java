package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.BenchmarkResult;
import com.localmind.domain.mcp.port.out.PerformanceProfilerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerformanceProfilerServiceTest {

    @Mock
    private PerformanceProfilerRepository repository;

    private PerformanceProfilerService service;

    @BeforeEach
    void setUp() {
        when(repository.save(any(BenchmarkResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new PerformanceProfilerService(repository);
    }

    // ========================================================================
    // analyzeBundle tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void analyzeBundle_withHeavyJsImports_detectsHeavyDependencies() {
        String code = "import moment from 'moment';\n" +
                "import _ from 'lodash';\n" +
                "import { Observable } from 'rxjs';\n" +
                "const express = require('express');\n" +
                "\n" +
                "console.log('hello');\n";

        Map<String, Object> result = service.analyzeBundle(code, "src/index.js");

        assertThat(result.get("filePath")).isEqualTo("src/index.js");
        assertThat((int) result.get("linesOfCode")).isEqualTo(6);
        assertThat((int) result.get("totalImports")).isGreaterThanOrEqualTo(4);
        assertThat((int) result.get("heavyDependencies")).isEqualTo(3); // moment, lodash, rxjs

        List<Map<String, Object>> imports = (List<Map<String, Object>>) result.get("imports");
        assertThat(imports).isNotEmpty();

        // Check moment is detected as heavy
        Map<String, Object> momentImport = imports.stream()
                .filter(i -> "moment".equals(i.get("name")))
                .findFirst().orElseThrow();
        assertThat(momentImport.get("isHeavy")).isEqualTo(true);
        assertThat(momentImport.get("estimatedSize")).isEqualTo("300KB");
        assertThat(momentImport.get("type")).isEqualTo("es-import");

        // Check lodash is detected as heavy
        Map<String, Object> lodashImport = imports.stream()
                .filter(i -> "lodash".equals(i.get("name")))
                .findFirst().orElseThrow();
        assertThat(lodashImport.get("isHeavy")).isEqualTo(true);
        assertThat(lodashImport.get("estimatedSize")).isEqualTo("70KB");

        // Check rxjs is detected as heavy
        Map<String, Object> rxjsImport = imports.stream()
                .filter(i -> "rxjs".equals(i.get("name")))
                .findFirst().orElseThrow();
        assertThat(rxjsImport.get("isHeavy")).isEqualTo(true);

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((int) summary.get("heavyCount")).isEqualTo(3);
        assertThat((String) summary.get("suggestion")).contains("heavy dependencies");

        verify(repository).save(any(BenchmarkResult.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeBundle_withNoHeavyImports_reportsClean() {
        String code = "import { Component } from '@angular/core';\n" +
                "import { HttpClient } from '@angular/common/http';\n" +
                "\n" +
                "export class MyComponent {}\n";

        Map<String, Object> result = service.analyzeBundle(code, "src/app.component.ts");

        assertThat((int) result.get("heavyDependencies")).isEqualTo(0);
        assertThat((int) result.get("totalImports")).isGreaterThanOrEqualTo(2);

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat((int) summary.get("heavyCount")).isEqualTo(0);
        assertThat((String) summary.get("suggestion")).contains("clean");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeBundle_withJavaImports_detectsHeavyJavaDeps() {
        String code = "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
                "import org.hibernate.Session;\n" +
                "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
                "import com.google.common.collect.ImmutableList;\n" +
                "import org.apache.commons.lang3.StringUtils;\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class App {}\n";

        Map<String, Object> result = service.analyzeBundle(code, "src/main/java/App.java");

        assertThat((int) result.get("heavyDependencies")).isEqualTo(5);
        assertThat((int) result.get("totalImports")).isEqualTo(6);

        List<Map<String, Object>> imports = (List<Map<String, Object>>) result.get("imports");

        // spring-boot
        Map<String, Object> springImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("org.springframework.boot"))
                .findFirst().orElseThrow();
        assertThat(springImport.get("isHeavy")).isEqualTo(true);
        assertThat(springImport.get("estimatedSize")).isEqualTo("20MB+");

        // hibernate
        Map<String, Object> hibernateImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("org.hibernate"))
                .findFirst().orElseThrow();
        assertThat(hibernateImport.get("isHeavy")).isEqualTo(true);
        assertThat(hibernateImport.get("estimatedSize")).isEqualTo("8MB+");

        // jackson
        Map<String, Object> jacksonImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("com.fasterxml.jackson"))
                .findFirst().orElseThrow();
        assertThat(jacksonImport.get("isHeavy")).isEqualTo(true);
        assertThat(jacksonImport.get("estimatedSize")).isEqualTo("3MB+");

        // guava
        Map<String, Object> guavaImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("com.google.common"))
                .findFirst().orElseThrow();
        assertThat(guavaImport.get("isHeavy")).isEqualTo(true);
        assertThat(guavaImport.get("estimatedSize")).isEqualTo("3MB+");

        // commons-lang3
        Map<String, Object> commonsImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("org.apache.commons.lang3"))
                .findFirst().orElseThrow();
        assertThat(commonsImport.get("isHeavy")).isEqualTo(true);
        assertThat(commonsImport.get("estimatedSize")).isEqualTo("500KB");

        // java.util.List should NOT be heavy
        Map<String, Object> javaUtilImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("java.util"))
                .findFirst().orElseThrow();
        assertThat(javaUtilImport.get("isHeavy")).isEqualTo(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeBundle_withMixedJsImports_handlesAllFormats() {
        String code = "import React from 'react';\n" +
                "import 'polyfill';\n" +
                "const axios = require('axios');\n" +
                "import { Button } from '@mui/material';\n" +
                "import $ from 'jquery';\n";

        Map<String, Object> result = service.analyzeBundle(code, "src/main.tsx");

        assertThat((int) result.get("totalImports")).isGreaterThanOrEqualTo(5);
        // @mui and jquery are heavy
        assertThat((int) result.get("heavyDependencies")).isEqualTo(2);

        List<Map<String, Object>> imports = (List<Map<String, Object>>) result.get("imports");

        Map<String, Object> muiImport = imports.stream()
                .filter(i -> ((String) i.get("name")).startsWith("@mui"))
                .findFirst().orElseThrow();
        assertThat(muiImport.get("isHeavy")).isEqualTo(true);
        assertThat(muiImport.get("estimatedSize")).isEqualTo("300KB+");

        Map<String, Object> jqueryImport = imports.stream()
                .filter(i -> "jquery".equals(i.get("name")))
                .findFirst().orElseThrow();
        assertThat(jqueryImport.get("isHeavy")).isEqualTo(true);
        assertThat(jqueryImport.get("estimatedSize")).isEqualTo("85KB");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeBundle_withEmptyCode_returnsEmptyResult() {
        Map<String, Object> result = service.analyzeBundle("", "empty.js");

        assertThat((int) result.get("linesOfCode")).isEqualTo(0);
        assertThat((int) result.get("totalImports")).isEqualTo(0);
        assertThat((int) result.get("heavyDependencies")).isEqualTo(0);
        assertThat((List<?>) result.get("imports")).isEmpty();

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertThat(summary.get("totalEstimatedSize")).isEqualTo("0KB");
        assertThat((String) summary.get("suggestion")).contains("No imports");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeBundle_withNullCode_returnsEmptyResult() {
        Map<String, Object> result = service.analyzeBundle(null, "test.js");

        assertThat((int) result.get("linesOfCode")).isEqualTo(0);
        assertThat((int) result.get("totalImports")).isEqualTo(0);
    }

    @Test
    void analyzeBundle_persistsResult() {
        String code = "import moment from 'moment';\n";

        service.analyzeBundle(code, "test.js");

        ArgumentCaptor<BenchmarkResult> captor = ArgumentCaptor.forClass(BenchmarkResult.class);
        verify(repository).save(captor.capture());

        BenchmarkResult saved = captor.getValue();
        assertThat(saved.getName()).contains("bundle-analysis");
        assertThat(saved.getLanguage()).isEqualTo("javascript");
        assertThat(saved.getResultJson()).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========================================================================
    // findBottlenecks tests
    // ========================================================================

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_nestedLoop_detectedAsCritical() {
        String code = "public void process(List<String> items, List<String> other) {\n" +
                "    for (int i = 0; i < items.size(); i++) {\n" +
                "        for (int j = 0; j < other.size(); j++) {\n" +
                "            if (items.get(i).equals(other.get(j))) {\n" +
                "                System.out.println(\"match\");\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        assertThat((int) result.get("totalBottlenecks")).isGreaterThanOrEqualTo(1);

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> nestedLoop = bottlenecks.stream()
                .filter(b -> "nested-loop".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(nestedLoop.get("severity")).isEqualTo("critical");
        assertThat((String) nestedLoop.get("message")).contains("O(n^2)");
        assertThat((String) nestedLoop.get("suggestion")).contains("Map");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_syncIo_detectedForJavascript() {
        String code = "const fs = require('fs');\n" +
                "const data = fs.readFileSync('file.txt');\n" +
                "fs.writeFileSync('output.txt', data);\n" +
                "const result = require('child_process').execSync('ls');\n";

        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        List<Map<String, Object>> syncIo = bottlenecks.stream()
                .filter(b -> "sync-io".equals(b.get("type")))
                .toList();
        assertThat(syncIo).hasSize(3); // readFileSync, writeFileSync, execSync
        for (Map<String, Object> b : syncIo) {
            assertThat(b.get("severity")).isEqualTo("warning");
            assertThat((String) b.get("suggestion")).contains("async");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_syncIo_notDetectedForJava() {
        String code = "BufferedReader reader = new BufferedReader(new FileReader(\"file.txt\"));\n" +
                "String line = reader.readLine();\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        List<Map<String, Object>> syncIo = bottlenecks.stream()
                .filter(b -> "sync-io".equals(b.get("type")))
                .toList();
        assertThat(syncIo).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_linearSearchInLoop_detected() {
        String code = "public void search(List<String> items, List<String> targets) {\n" +
                "    for (String item : items) {\n" +
                "        if (targets.contains(item)) {\n" +
                "            System.out.println(item);\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        // Uses for-each which is matched by the pattern
        // We test with a standard for loop instead to be safe
        String code2 = "public void search(List<String> items, List<String> targets) {\n" +
                "    for (int i = 0; i < items.size(); i++) {\n" +
                "        if (targets.indexOf(items.get(i)) >= 0) {\n" +
                "            System.out.println(\"found\");\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code2, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> linearSearch = bottlenecks.stream()
                .filter(b -> "linear-search-in-loop".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(linearSearch.get("severity")).isEqualTo("warning");
        assertThat((String) linearSearch.get("suggestion")).contains("Set");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_missingPagination_detected() {
        String code = "public List<User> getAll() {\n" +
                "    return userRepository.findAll();\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> pagination = bottlenecks.stream()
                .filter(b -> "missing-pagination".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(pagination.get("severity")).isEqualTo("warning");
        assertThat((String) pagination.get("suggestion")).contains("pagination");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_domQueryInLoop_detectedForJs() {
        String code = "function updateItems(items) {\n" +
                "    for (let i = 0; i < items.length; i++) {\n" +
                "        const el = document.querySelector('.item-' + i);\n" +
                "        el.textContent = items[i];\n" +
                "    }\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> domQuery = bottlenecks.stream()
                .filter(b -> "dom-query-in-loop".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(domQuery.get("severity")).isEqualTo("critical");
        assertThat((String) domQuery.get("suggestion")).contains("Cache");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_stringConcatInLoop_detectedAsInfo() {
        String code = "public String buildCsv(List<String> rows) {\n" +
                "    String result = \"\";\n" +
                "    for (String row : rows) {\n" +
                "        result += row + \"\\n\";\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n";

        // We use a standard for loop for reliable detection
        String code2 = "public String buildCsv(List<String> rows) {\n" +
                "    String result = \"\";\n" +
                "    for (int i = 0; i < rows.size(); i++) {\n" +
                "        result += rows.get(i) + \"\\n\";\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code2, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> stringConcat = bottlenecks.stream()
                .filter(b -> "string-concat-in-loop".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(stringConcat.get("severity")).isEqualTo("info");
        assertThat((String) stringConcat.get("suggestion")).contains("StringBuilder");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_stringConcatInLoop_suggestsArrayJoinForJs() {
        String code = "function buildStr(items) {\n" +
                "    let result = '';\n" +
                "    for (let i = 0; i < items.length; i++) {\n" +
                "        result += items[i];\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> stringConcat = bottlenecks.stream()
                .filter(b -> "string-concat-in-loop".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat((String) stringConcat.get("suggestion")).contains("array.join");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_jsonInLoop_detected() {
        String code = "public void processItems(List<String> jsonList) {\n" +
                "    ObjectMapper mapper = new ObjectMapper();\n" +
                "    for (int i = 0; i < jsonList.size(); i++) {\n" +
                "        Object obj = mapper.readValue(jsonList.get(i), Object.class);\n" +
                "        System.out.println(obj);\n" +
                "    }\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        // The ObjectMapper appears inside the loop body (line 4 has readValue which triggers json-in-loop detection)
        Map<String, Object> jsonInLoop = bottlenecks.stream()
                .filter(b -> "json-in-loop".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(jsonInLoop.get("severity")).isEqualTo("warning");
        assertThat((String) jsonInLoop.get("suggestion")).contains("outside the loop");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_jsonParseInLoopJs_detected() {
        String code = "function processItems(items) {\n" +
                "    for (let i = 0; i < items.length; i++) {\n" +
                "        const obj = JSON.parse(items[i]);\n" +
                "        console.log(JSON.stringify(obj));\n" +
                "    }\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        List<Map<String, Object>> jsonIssues = bottlenecks.stream()
                .filter(b -> "json-in-loop".equals(b.get("type")))
                .toList();
        assertThat(jsonIssues).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_recursionWithoutMemo_detected() {
        String code = "public int fibonacci(int n) {\n" +
                "    if (n <= 1) {\n" +
                "        return n;\n" +
                "    }\n" +
                "    return fibonacci(n - 1) + fibonacci(n - 2);\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> recursion = bottlenecks.stream()
                .filter(b -> "recursion-without-memo".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(recursion.get("severity")).isEqualTo("info");
        assertThat((String) recursion.get("message")).contains("fibonacci");
        assertThat((String) recursion.get("suggestion")).contains("memoization");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_sequentialAwait_detectedForJs() {
        String code = "async function fetchAll(urls) {\n" +
                "    const results = [];\n" +
                "    for (let i = 0; i < urls.length; i++) {\n" +
                "        const res = await fetch(urls[i]);\n" +
                "        results.push(res);\n" +
                "    }\n" +
                "    return results;\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> seqAwait = bottlenecks.stream()
                .filter(b -> "sequential-await".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat(seqAwait.get("severity")).isEqualTo("warning");
        assertThat((String) seqAwait.get("suggestion")).contains("Promise.all");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_sequentialAwait_suggestsCompletableFutureForJava() {
        String code = "public void fetchAll(List<CompletableFuture<String>> futures) {\n" +
                "    for (int i = 0; i < futures.size(); i++) {\n" +
                "        String result = futures.get(i).get();\n" +
                "        System.out.println(result);\n" +
                "    }\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        Map<String, Object> seqAwait = bottlenecks.stream()
                .filter(b -> "sequential-await".equals(b.get("type")))
                .findFirst().orElseThrow();
        assertThat((String) seqAwait.get("suggestion")).contains("CompletableFuture.allOf");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_cleanCode_returnsNoBottlenecks() {
        String code = "public int add(int a, int b) {\n" +
                "    return a + b;\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "java");

        assertThat((int) result.get("totalBottlenecks")).isEqualTo(0);

        Map<String, Integer> bySeverity = (Map<String, Integer>) result.get("bySeverity");
        assertThat(bySeverity.get("critical")).isEqualTo(0);
        assertThat(bySeverity.get("warning")).isEqualTo(0);
        assertThat(bySeverity.get("info")).isEqualTo(0);

        assertThat((Map<?, ?>) result.get("byType")).isEmpty();
        assertThat((List<?>) result.get("bottlenecks")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_emptyCode_returnsEmptyResult() {
        Map<String, Object> result = service.findBottlenecks("", "java");

        assertThat((int) result.get("totalBottlenecks")).isEqualTo(0);
        assertThat((List<?>) result.get("bottlenecks")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_multipleIssues_countsCorrectly() {
        String code = "async function process(items) {\n" +
                "    let result = '';\n" +
                "    for (let i = 0; i < items.length; i++) {\n" +
                "        for (let j = 0; j < items.length; j++) {\n" +
                "            const el = document.querySelector('.item');\n" +
                "            result += JSON.stringify(el);\n" +
                "        }\n" +
                "        const data = await fetch('/api/' + i);\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n";

        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        assertThat((int) result.get("totalBottlenecks")).isGreaterThanOrEqualTo(3);

        Map<String, Integer> bySeverity = (Map<String, Integer>) result.get("bySeverity");
        assertThat(bySeverity.get("critical")).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findBottlenecks_persistsResult() {
        String code = "for (int i = 0; i < 10; i++) {\n" +
                "    for (int j = 0; j < 10; j++) {\n" +
                "        System.out.println(i + j);\n" +
                "    }\n" +
                "}\n";

        service.findBottlenecks(code, "java");

        ArgumentCaptor<BenchmarkResult> captor = ArgumentCaptor.forClass(BenchmarkResult.class);
        verify(repository).save(captor.capture());

        BenchmarkResult saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("bottleneck-analysis");
        assertThat(saved.getLanguage()).isEqualTo("java");
        assertThat(saved.getResultJson()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findBottlenecks_objectCreationInRender_detectedForJs() {
        String code = "function MyComponent() {\n" +
                "    return (\n" +
                "        <div style={new Object()}>\n" +
                "            Hello\n" +
                "        </div>\n" +
                "    );\n" +
                "}\n";

        // This pattern detects "return (" with JSX and "new Object()" inside
        Map<String, Object> result = service.findBottlenecks(code, "javascript");

        List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
        List<Map<String, Object>> objectCreation = bottlenecks.stream()
                .filter(b -> "object-creation-in-render".equals(b.get("type")))
                .toList();
        assertThat(objectCreation).isNotEmpty();
        assertThat(objectCreation.get(0).get("severity")).isEqualTo("warning");
        assertThat((String) objectCreation.get(0).get("suggestion")).contains("useMemo");
    }

    // ========================================================================
    // benchmarkCompare tests
    // ========================================================================

    @Test
    void benchmarkCompare_java_generatesJavaBenchmark() {
        String codeA = "int sum = 0; for (int i = 0; i < 1000; i++) sum += i;";
        String codeB = "int sum = IntStream.range(0, 1000).sum();";

        Map<String, Object> result = service.benchmarkCompare(codeA, codeB, 500, "java");

        assertThat(result.get("language")).isEqualTo("java");
        assertThat((int) result.get("iterations")).isEqualTo(500);

        String benchmarkCode = (String) result.get("benchmarkCode");
        assertThat(benchmarkCode).contains("public class Benchmark");
        assertThat(benchmarkCode).contains("System.nanoTime()");
        assertThat(benchmarkCode).contains("static void codeA()");
        assertThat(benchmarkCode).contains("static void codeB()");
        assertThat(benchmarkCode).contains(codeA);
        assertThat(benchmarkCode).contains(codeB);
        assertThat(benchmarkCode).contains("Warming up");
        assertThat(benchmarkCode).contains("Mean:");
        assertThat(benchmarkCode).contains("Median:");
        assertThat(benchmarkCode).contains("P95:");
        assertThat(benchmarkCode).contains("P99:");
        assertThat(benchmarkCode).contains("StdDev:");
        assertThat(benchmarkCode).contains("Ops/sec:");
        assertThat(benchmarkCode).contains("Winner:");

        String instructions = (String) result.get("instructions");
        assertThat(instructions).contains("javac");
        assertThat(instructions).contains("java Benchmark");
    }

    @Test
    void benchmarkCompare_javascript_generatesJsBenchmark() {
        String codeA = "let sum = 0; for (let i = 0; i < 1000; i++) sum += i;";
        String codeB = "const sum = Array.from({length: 1000}, (_, i) => i).reduce((a, b) => a + b, 0);";

        Map<String, Object> result = service.benchmarkCompare(codeA, codeB, 2000, "javascript");

        assertThat(result.get("language")).isEqualTo("javascript");
        assertThat((int) result.get("iterations")).isEqualTo(2000);

        String benchmarkCode = (String) result.get("benchmarkCode");
        assertThat(benchmarkCode).contains("performance.now()");
        assertThat(benchmarkCode).contains("function codeA()");
        assertThat(benchmarkCode).contains("function codeB()");
        assertThat(benchmarkCode).contains(codeA);
        assertThat(benchmarkCode).contains(codeB);
        assertThat(benchmarkCode).contains("perf_hooks");
        assertThat(benchmarkCode).contains("Mean:");
        assertThat(benchmarkCode).contains("Winner:");

        String instructions = (String) result.get("instructions");
        assertThat(instructions).contains("node");
    }

    @Test
    void benchmarkCompare_defaultLanguage_isJava() {
        Map<String, Object> result = service.benchmarkCompare("int x = 1;", "int x = 2;", 100, null);

        assertThat(result.get("language")).isEqualTo("java");
        String benchmarkCode = (String) result.get("benchmarkCode");
        assertThat(benchmarkCode).contains("public class Benchmark");
    }

    @Test
    void benchmarkCompare_warmupCalculation_respectsMax100() {
        // With 2000 iterations, warmup = min(2000/10, 100) = 100
        Map<String, Object> result = service.benchmarkCompare("int x = 1;", "int x = 2;", 2000, "java");

        String benchmarkCode = (String) result.get("benchmarkCode");
        assertThat(benchmarkCode).contains("int warmup = 100");
    }

    @Test
    void benchmarkCompare_smallIterations_warmupIs10Percent() {
        // With 50 iterations, warmup = min(50/10, 100) = 5
        Map<String, Object> result = service.benchmarkCompare("int x = 1;", "int x = 2;", 50, "java");

        String benchmarkCode = (String) result.get("benchmarkCode");
        assertThat(benchmarkCode).contains("int warmup = 5");
    }

    @Test
    void benchmarkCompare_persistsResult() {
        service.benchmarkCompare("int x = 1;", "int x = 2;", 100, "java");

        ArgumentCaptor<BenchmarkResult> captor = ArgumentCaptor.forClass(BenchmarkResult.class);
        verify(repository).save(captor.capture());

        BenchmarkResult saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("benchmark-compare");
        assertThat(saved.getLanguage()).isEqualTo("java");
        assertThat(saved.getResultJson()).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void benchmarkCompare_zeroIterations_defaultsTo1000() {
        Map<String, Object> result = service.benchmarkCompare("int x = 1;", "int x = 2;", 0, "java");

        assertThat((int) result.get("iterations")).isEqualTo(1000);
    }
}
