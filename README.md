  ## Here are the steps to prepare the data 
  Given cryptokitty transaction as the example
  1. for the rpc call use "trace_transaction" as the method
  2. get the colorful txt result of the trace using foundry tool
  3. use the uploaded "substitute_types.py" to substitute the function call method with the actual function names
  4. attach_money_flows.py is attaching the money flow result generate by money_flow_analyzer.py
  5. to_trace_event.py is converting the result to the trace event format
  6. to_trace_event_with_timestamps.py is inserting the timestamps


# TxSight — User & Build Guide

## 1. Using the Prebuilt Version (Release)

### Prerequisites

- Linux system (x86_64) : tested on Ubuntu 24.04.2 LTS x86_64 with Linux kernel 6.11.0-26-generic and Fedora Linux 42 KDE Plasma Desktop Edition with Linux kernel 6.14.0-63 x86_64 
- GTK libraries (required for SWT-based applications)

### Launching the Application

1. Ensure the executable file (e.g., `./tracecompass`) is present in the root directory.
2. If it's not executable, make it so:

    ```bash
    chmod +x ./tracecompass
    ```

3. Then launch the application:

    ```bash
    ./tracecompass
    ```

### Enabling the Trace Event Parser Plugin

Once the application is running:

- Go to **Tools > Add-ons**.
- Check the box for **Trace Event Parser** to enable the plugin.

### Adding the Gas Usage Analysis View

1. In the **Project Explorer**, right-click on `Traces`.
2. Select **Manage XML Analyses**.
3. In the dialog, click **Import** and choose `gas_cost_per_function.xml` located in the `addFiles` directory.

### Opening a Trace

- Open the following trace files from the `addFiles` folder:
  - `trace_event_crypto_kitty.json`
  - `trace_event_tsDao.json`
  - `0x55a72f6c7608257afed88cd423e050368c0e3b2cba94a23c51ab811827b89f01.json`
  
- Ensure that the corresponding storage report file is also present for each trace:
  - `trace_event_crypto_kitty_storage_report.json`
  - `trace_event_tsDao_storage_report.json`
  - `0x55a72f6c7608257afed88cd423e050368c0e3b2cba94a23c51ab811827b89f01_storage_report.json`

These storage report files are required for analyzing state changes.

## 2. Building the Project from Source

### Prerequisites

- Java JDK 21 : tested with openjdk 21.0.7 2025-04-15
- Maven 3.9+ : tested with Apache Maven 3.9.9
- Git
- Linux system (x86_64) : tested on Ubuntu 24.04.2 LTS x86_64 with Linux kernel 6.11.0-26-generic
- GTK libraries

### Building without Running Tests

To build the project without running unit tests, run the following in the project root:

```bash
mvn clean install -Dmaven.test.skip=true -DskipTests
```

This command:

* Cleans previous build files
* Compiles the project
* Skips tests to speed up the process

## Run the Compiled TxSight

After a successful build, you can run TxSight using the following command:

```bash
./rcp/org.eclipse.tracecompass.rcp.product/target/products/org.eclipse.tracecompass.rcp/linux/gtk/x86_64/trace-compass/tracecompass
```

 🔧 Once TxSight is launched, make sure to enable the **Trace Event Parser** plugin by going to **Tools > Add-ons** and checking **Trace Event Parser**.

### Notes:

* Ensure the file is executable. If not, you can make it executable with:

  ```bash
  chmod +x ./rcp/org.eclipse.tracecompass.rcp.product/target/products/org.eclipse.tracecompass.rcp/linux/gtk/x86_64/trace-compass/tracecompass
  ```

* The path may vary slightly depending on the project version or Maven configuration.

## Troubleshooting

* If the build fails, check that all dependencies are properly resolved (common issues: `zest`, `jdt.annotation`, etc.).
* To force Maven to update dependencies:

  ```bash
  mvn clean install -U -Dmaven.test.skip=true -DskipTests
  ```

## License

This project is licensed under the [Eclipse Public License v2.0](https://www.eclipse.org/legal/epl-2.0/).
