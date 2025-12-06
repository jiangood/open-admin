# Project Overview

This project is a Spring Boot application with a React frontend.

## Project Introduction

`springboot-admin-starter` is a powerful enterprise application quick development starter kit, aiming to provide a ready-made basic platform integrated with common backend management functions. It adopts a
**前后端分离 (frontend-backend separation)** architecture, with the **backend based on Spring Boot** and the **frontend based on Ant Design**. It helps developers quickly start new projects and focus on
the implementation of business logic.

## Features List

*   System Management
*   Organization Management
*   User Management
*   Role Management
*   Operation Manual
*   System Configuration
*   Data Dictionary
*   File Management
*   Operation Log
*   API Management
*   Job Scheduling
*   Workflow Engine
*   Report Management

## Backend (Maven/Spring Boot)

*   **Name:** springboot-admin-starter
*   **GroupId:** io.github.jiangood
*   **ArtifactId:** springboot-admin-starter
*   **Version:** 0.0.2
*   **Description:** springboot-admin-starter
*   **Java Version:** 17
*   **Main Dependencies:**
    *   `org.springframework.boot:spring-boot-starter-web`
    *   `org.springframework.boot:spring-boot-starter-quartz`
    *   `org.springframework.boot:spring-boot-starter-validation`
    *   `org.springframework.boot:spring-boot-starter-data-jpa`
    *   `org.springframework.boot:spring-boot-starter-security`
    *   `cn.hutool:hutool-all`
    *   `io.minio:minio`
    *   `org.flowable:flowable-spring-boot-starter-process`

## Frontend (npm/React)

*   **Name:** @jiangood/springboot-admin-starter
*   **Version:** 0.0.1-beta.30
*   **Description:** springboot-admin-starter
*   **Main Dev Dependencies:**
    *   `@types/react`
    *   `@types/react-dom`
    *   `typescript`
    *   `@umijs/plugins`
*   **Main Peer Dependencies:**
    *   `antd`
    *   `react`
    *   `react-dom`
    *   `umi`
    *   `axios`
    *   `dayjs`
*   **Scripts:**
    *   `dev`: `umi dev`
    *   `build`: `tsc --outDir config/dist --skipLibCheck --noEmitOnError false config/index.ts`

### Frontend Framework Components (`web/src/framework/components`)

| Component Name       | Description                                                 |
| :------------------- | :---------------------------------------------------------- |
| `DownloadFileButton` | A button that triggers file download and shows loading state. |
| `LinkButton`         | A button used for page navigation.                          |
| `NamedIcon`          | Renders an Ant Design icon based on its name.               |
| `PageLoading`        | Displays a centered loading indicator when the page is loading. |

### Frontend Form Field Components (`web/src/framework/field-components`)

| Component Name              | Description                                                                                             |
| :-------------------------- | :------------------------------------------------------------------------------------------------------ |
| `FieldBoolean`              | Provides boolean input components with various styles (radio, checkbox, dropdown, switch).              |
| `FieldDate`                 | Renders a date or time selector based on the specified type (year, month, day, time, etc.).             |
| `FieldDateRange`            | Similar to `FieldDate`, but used for selecting a date or time range.                                    |
| `FieldDictSelect`           | A dropdown selector that loads options from a data dictionary.                                          |
| `FieldEditor`               | A rich text editor, based on TinyMCE.                                                                   |
| `FieldPercent`              | A component for entering percentage values, converting input between 0-100 and 0-1 ranges.            |
| `FieldRemoteSelect`         | A dropdown selector that asynchronously loads data from a remote URL.                                   |
| `FieldRemoteSelectMultiple` | Multi-select version of `FieldRemoteSelect`.                                                            |
| `FieldRemoteSelectMultipleInline` | A variant of `FieldRemoteSelectMultiple` whose value is a comma-separated string.                   |
| `FieldRemoteTree`           | A tree component that loads data from a remote URL, supporting multiple selections.                     |
| `FieldRemoteTreeCascader`   | A cascader that loads data from a remote URL.                                                           |
| `FieldRemoteTreeSelect`     | A tree-shaped dropdown selector that loads data from a remote URL.                                      |
| `FieldRemoteTreeSelectMultiple` | Multi-select version of `FieldRemoteTreeSelect`.                                                    |
| `FieldSysOrgTree`           | A tree component for displaying the system's organizational structure (departments or units).           |
| `FieldSysOrgTreeSelect`     | A tree-shaped dropdown selector for selecting the system's organizational structure.                    |
| `FieldTable`                | An editable table component that supports dynamic row addition and deletion.                            |
| `FieldTableSelect`          | A dropdown selector whose popup is a full-featured table, supporting search and pagination.             |
| `FieldUploadFile`           | A file upload component that supports image cropping and preview.                                       |

### Frontend View Components (`web/src/framework/view-components`)

| Component Name | Description                                         |
| :------------- | :-------------------------------------------------- |
| `ViewBoolean`  | Displays boolean values `true` and `false` as "是" (Yes) and "否" (No). |
| `ViewPassword` | Used to display passwords, with a toggle to switch between masked and plain text. |

### Frontend Utility Classes (`web/src/framework/utils`)

| Utility Class Name | Description                                                                         |
| :----------------- | :---------------------------------------------------------------------------------- |
| `ArrUtils`         | Provides a series of static methods for array operations, such as adding, deleting, and searching. |
| `ColorsUtils`      | A utility class for color conversion and manipulation, supporting RGB, HSV, and Hex formats. |
| `DateUtils`        | Provides date formatting, parsing, and human-friendly time display functions.       |
| `DeviceUtils`      | Used to detect device types (e.g., mobile devices) and get environment information (e.g., WebSocket URL). |
| `DomUtils`         | Provides methods for getting DOM element dimensions and positions.                  |
| `EventBusUtils`    | A static global event bus for in-application message passing.                       |
| `MessageUtils`     | Encapsulates Ant Design's `Modal`, `message`, and `notification`, providing a unified API. |
| `ObjectUtils`      | Provides methods for safely getting and copying object properties.                  |
| `StorageUtils`     | Encapsulates `localStorage`, providing timestamped data storage and retrieval functions. |
| `StringUtils`      | Provides rich string operation methods, such as case conversion, padding, encryption/decryption, etc. |
| `TreeUtils`        | Used for conversion, traversal, and search operations of tree-like data structures. |
| `UrlUtils`         | Provides functions for manipulating URL parameters, path concatenation, etc.        |
| `UuidUtils`        | Used for generating v4 UUIDs.                                                       |
| `ValidateUtils`    | Provides data validation functions, currently supporting email format validation.   |