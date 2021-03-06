/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.dynatrace;

import io.micrometer.core.lang.Nullable;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * @author Oriol Barcelona
 */
class DynatraceMetricDefinition {

    /**
     * Subset of mappable units of the custom metric API.
     *
     * @see <a href="https://www.dynatrace.com/support/help/shortlink/api-custom-metrics#put-custom-metric">available units</a>
     */
    enum DynatraceUnit {
        // Time
        NanoSecond, MicroSecond, MilliSecond, Second,

        // Information
        Bit, Byte, KiloByte, KibiByte, MegaByte, MebiByte, GigaByte, GibiByte,

        // Count
        Count;

        private static Map<String, DynatraceUnit> UNITS_MAPPING = Collections.unmodifiableMap(
                Stream.of(DynatraceUnit.values()).collect(Collectors.toMap(k -> k.toString().toLowerCase() + "s", Function.identity())));

        static DynatraceUnit fromPlural(final String plural) {
            return UNITS_MAPPING.getOrDefault(plural, null);
        }
    }

    private final String metricId;

    @Nullable
    private final String description;

    @Nullable
    private final DynatraceUnit unit;

    @Nullable
    private final Set<String> dimensions;

    // Guaranteed to be non-empty
    private final String[] technologyTypes;

    DynatraceMetricDefinition(String metricId, @Nullable String description, @Nullable DynatraceUnit unit, @Nullable Set<String> dimensions,
                              String[] technologyTypes) {
        this.metricId = metricId;
        this.description = description;
        this.unit = unit;
        this.dimensions = dimensions;
        this.technologyTypes = technologyTypes;
    }

    String getMetricId() {
        return metricId;
    }

    String asJson() {
        String displayName = description == null ? metricId : StringEscapeUtils.escapeJson(description);
        if(displayName.length() > 256)
            displayName = displayName.substring(0, 256);

        String body = "{\"displayName\":\"" + displayName + "\"";

        if (unit != null)
            body += ",\"unit\":\"" + unit + "\"";

        if (dimensions != null && !dimensions.isEmpty())
            body += ",\"dimensions\":[" + dimensions.stream()
                    .map(d -> "\"" + d + "\"")
                    .collect(Collectors.joining(",")) + "]";

        body += ",\"types\":[" +
                stream(technologyTypes).map(type -> "\"" + type + "\"").collect(Collectors.joining(",")) +
                "]";

        body += "}";
        return body;
    }
}
