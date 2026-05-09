package com.fz.vibeverse.system.exchange.support;

import com.fz.vibeverse.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 导入导出 CSV 编解码。
 */
@Component
public class CsvExchangeCodec {

    public List<DataImportRow> readRows(InputStream inputStream, List<String> expectedHeaders) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw ApiException.badRequest("导入文件不能为空");
            }
            validateHeaders(parseLine(stripBom(headerLine)), expectedHeaders);

            List<DataImportRow> rows = new ArrayList<>();
            String line;
            int rowNo = 1;
            while ((line = reader.readLine()) != null) {
                rowNo++;
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                rows.add(new DataImportRow(rowNo, parseLine(line)));
            }
            return rows;
        } catch (IOException ex) {
            throw ApiException.business("读取导入文件失败：" + ex.getMessage());
        }
    }

    public byte[] buildTemplate(List<String> headers, List<List<String>> sampleRows) {
        return writeRows(headers, sampleRows).getBytes(StandardCharsets.UTF_8);
    }

    public String writeRows(List<String> headers, List<? extends List<?>> rows) {
        StringBuilder builder = new StringBuilder("\uFEFF");
        appendRow(builder, headers);
        if (rows != null) {
            for (List<?> row : rows) {
                appendRow(builder, row);
            }
        }
        return builder.toString();
    }

    public String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private void appendRow(StringBuilder builder, List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(csv(values.get(i)));
        }
        builder.append('\n');
    }

    private void validateHeaders(List<String> actualHeaders, List<String> expectedHeaders) {
        if (actualHeaders == null || expectedHeaders == null || actualHeaders.size() < expectedHeaders.size()) {
            throw ApiException.badRequest("导入模板表头不正确");
        }
        for (int i = 0; i < expectedHeaders.size(); i++) {
            if (!Objects.equals(expectedHeaders.get(i), normalizeOptional(actualHeaders.get(i)))) {
                throw ApiException.badRequest("导入模板表头不正确，请先下载最新模板");
            }
        }
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String stripBom(String input) {
        if (input != null && input.startsWith("\uFEFF")) {
            return input.substring(1);
        }
        return input;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }
}
