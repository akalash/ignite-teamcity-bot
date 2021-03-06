/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ci.util;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by dpavlov on 02.08.2017
 */
public class Base64Util {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static String encodeUtf8String(String data) {
        return encodeBytesToString(data.getBytes(CHARSET));
    }

    @NotNull
    public static String encodeBytesToString(byte[] bytes) {
        final byte[] encode = Base64.getEncoder().encode(bytes);
        return new String(encode, CHARSET);
    }

    @NotNull
    public static byte[] decodeString(String string) {
        return Base64.getDecoder().decode(string.getBytes(CHARSET));
    }

}
