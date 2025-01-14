/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lexer grammar Keyword;

import Alphabet;

WS
    : [ \t\r\n] + ->skip
    ;

SHOW
    : S H O W
    ;

RULE
    : R U L E
    ;

SQL_TRANSLATOR
    : S Q L UL_ T R A N S L A T O R
    ;

USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED
    : U S E UL_ O R I G I N A L UL_ S Q L UL_ W H E N UL_ T R A N S L A T I N G UL_ F A I L E D
    ;

ALTER
    : A L T E R
    ;

TYPE
    : T Y P E
    ;

JOOQ
    : J O O Q
    ;

NATIVE
    : N A T I V E
    ;

NAME
    : N A M E
    ;

PROPERTIES
    : P R O P E R T I E S
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;
