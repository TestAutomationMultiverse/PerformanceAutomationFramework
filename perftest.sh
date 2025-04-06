#!/bin/bash

# ANSI color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Print script banner
print_banner() {
    echo "==========================================="
    echo "      Performance Automation Framework     "
    echo "==========================================="
}

# Check if Java is installed and at least version 17
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}Java is not installed. Please install Java 17 or higher.${NC}"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    java_major_version=$(echo "$java_version" | cut -d'.' -f1)
    
    if [ "$java_major_version" -lt 17 ]; then
        echo -e "${RED}Java version $java_version is not supported. Please install Java 17 or higher.${NC}"
        exit 1
    fi
}

# Print help message
print_help() {
    echo -e "${BLUE}Usage: ./perftest.sh COMMAND [OPTIONS]${NC}"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo "  setup             : Check and set up environment dependencies"
    echo "  build             : Build the project"
    echo "  run [test_type]   : Run tests"
    echo "  report [options]  : Generate or view reports"
    echo "  clean             : Clean target directory"
    echo "  help              : Show this help message"
    echo ""
    echo -e "${YELLOW}Test Types:${NC}"
    echo "  simple             : Run a simple test"
    echo "  http               : Run HTTP tests"
    echo "  graphql            : Run GraphQL tests"
    echo "  jdbc               : Run JDBC tests"
    echo "  soap               : Run SOAP tests"
    echo "  all                : Run all tests"
    echo "  html-demo          : Run a simple HTML report demo"
    echo "  full-demo          : Run a full feature demo"
    echo ""
    echo -e "${YELLOW}Report Options:${NC}"
    echo "  --view [report_path] : View a specific unified report"
    echo "  --list                : List all available report directories"
    echo "  --generate --path=DIR : Generate HTML report from JTL file in the specified directory"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  ./perftest.sh setup"
    echo "  ./perftest.sh build"
    echo "  ./perftest.sh run http"
    echo "  ./perftest.sh run html-demo"
    echo "  ./perftest.sh report --view target/unified-reports/http_20250406_132029_abcdef12"
    echo "  ./perftest.sh report --list"
    echo "  ./perftest.sh report --generate --path=target/unified-reports/http_20250406_132029_abcdef12"
    echo "  ./perftest.sh clean"
}

# Set up development environment
setup() {
    echo -e "${BLUE}Checking development environment...${NC}"
    
    # Check if Java is installed
    check_java
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Maven is not installed. Please install Maven 3.8 or higher.${NC}"
        exit 1
    fi
    
    # Check for other dependencies
    # ...
    
    echo -e "${GREEN}Development environment is ready!${NC}"
}

# Build the project
build_project() {
    echo -e "${BLUE}Building the project...${NC}"
    mvn clean install -DskipTests
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Build completed successfully!${NC}"
    else
        echo -e "${RED}Build failed!${NC}"
        exit 1
    fi
}

# Run tests with standardized JTL output
run_tests() {
    local test_type=$1
    
    echo -e "${BLUE}Running tests: $test_type${NC}"
    
    case $test_type in
        "simple")
            protocol="simple"
            mvn test -Dtest=SimpleTest 
            ;;
        "http")
            protocol="http"
            mvn test -Dtest=*Http*Test
            ;;
        "graphql")
            protocol="graphql"
            mvn test -Dtest=*GraphQL*Test
            ;;
        "jdbc")
            protocol="jdbc"
            mvn test -Dtest=*Jdbc*Test
            ;;
        "soap")
            protocol="soap"
            mvn test -Dtest=*Soap*Test
            ;;
        "all")
            protocol="all"
            mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO
            ;;
        "html-demo")
            run_simple_html_demo
            ;;
        "full-demo")
            run_html_report_demo
            ;;
        *)
            echo -e "${RED}Unknown test type: $test_type${NC}"
            print_help
            exit 1
            ;;
    esac
    
    # Find the most recently created unified report directory
    if [ -d "target/unified-reports" ]; then
        local latest_report=$(find "target/unified-reports" -maxdepth 1 -type d | sort -r | head -n 2 | tail -n 1)
        
        if [ -n "$latest_report" ] && [ -d "$latest_report" ]; then
            echo -e "${GREEN}Test execution completed! Results and reports available at: ${CYAN}$latest_report${NC}"
            echo -e "${YELLOW}To view the HTML report: ${CYAN}./perftest.sh report --view $latest_report${NC}"
        else
            echo -e "${GREEN}Test execution completed!${NC}"
        fi
    else
        echo -e "${GREEN}Test execution completed!${NC}"
    fi
}

# Run HTML report demo
run_html_report_demo() {
    echo -e "${BLUE}Running HTML report demo...${NC}"
    echo -e "${CYAN}This will create a unified report directory with JTL, HTML, and log files${NC}"
    
    # Run the demo class that works with JMeter DSL
    mvn test -Dtest=io.perftest.demo.HtmlReportDemoTest
}

# Run simple HTML report demo
run_simple_html_demo() {
    echo -e "${BLUE}Creating simple HTML report demo...${NC}"
    echo -e "${CYAN}This will create a unified report directory with JTL, HTML, and log files${NC}"
    
    # Run the compatible HTML demo class that works with JMeter DSL 1.29.1
    mvn test -Dtest=io.perftest.demo.compat.CompatHtmlReportDemo
}

# List all unified report directories
list_reports() {
    local unified_dir="target/unified-reports"
    
    if [ ! -d "$unified_dir" ] || [ -z "$(ls -A "$unified_dir")" ]; then
        echo -e "${YELLOW}No report directories found in $unified_dir${NC}"
        return
    fi
    
    echo -e "${BLUE}Available unified report directories:${NC}"
    echo -e "${CYAN}Directory${NC}                                 ${CYAN}JTL File${NC}       ${CYAN}HTML Report${NC}      ${CYAN}Log Files${NC}"
    echo "--------------------------------------------------------------------------------"
    
    for dir in "$unified_dir"/*; do
        if [ -d "$dir" ]; then
            # Count files in each category
            local jtl_count=$(find "$dir" -name "*.jtl" | wc -l)
            local html_count=$(find "$dir/html" -name "*.html" 2>/dev/null | wc -l)
            local log_count=$(find "$dir/logs" -name "*.log" 2>/dev/null | wc -l)
            
            # Directory name
            local dir_name=$(basename "$dir")
            
            # Print directory info with colored status indicators
            printf "%-40s " "$dir_name"
            
            if [ "$jtl_count" -gt 0 ]; then
                printf "${GREEN}✓ %-15s${NC}" "($jtl_count files)"
            else
                printf "${RED}✗ %-15s${NC}" "(Missing)"
            fi
            
            if [ "$html_count" -gt 0 ]; then
                printf "${GREEN}✓ %-15s${NC}" "($html_count files)"
            else
                printf "${RED}✗ %-15s${NC}" "(Missing)"
            fi
            
            if [ "$log_count" -gt 0 ]; then
                printf "${GREEN}✓ %-10s${NC}" "($log_count files)"
            else
                printf "${RED}✗ %-10s${NC}" "(Missing)"
            fi
            
            echo ""
        fi
    done
}

# Generate HTML report from JTL file in a unified report directory
generate_html_report() {
    local report_dir=$1
    
    if [ ! -d "$report_dir" ]; then
        echo -e "${RED}Report directory not found: $report_dir${NC}"
        exit 1
    fi
    
    # Find the JTL file in the report directory
    local jtl_file=$(find "$report_dir" -name "*.jtl" -type f | head -n 1)
    
    if [ -z "$jtl_file" ]; then
        echo -e "${RED}No JTL file found in: $report_dir${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}Generating HTML report from JTL file: $jtl_file${NC}"
    
    # Create the HTML report directory if it doesn't exist
    local html_dir="$report_dir/html"
    mkdir -p "$html_dir"
    
    # Generate report using Maven command
    mvn exec:java -Dexec.mainClass=io.perftest.reporting.JtlReportGenerator -Dexec.args="--input=$jtl_file --output=$html_dir"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}HTML report generated successfully at: $html_dir${NC}"
        echo -e "${BLUE}You can view the report with:${NC}"
        echo -e "  ./perftest.sh report --view $report_dir"
    else
        echo -e "${RED}HTML report generation failed!${NC}"
        exit 1
    fi
}

# View a specific unified report
view_report() {
    local report_dir=$1
    
    if [ ! -d "$report_dir" ]; then
        echo -e "${RED}Report directory not found: $report_dir${NC}"
        exit 1
    fi
    
    # Find HTML report index file
    local index_file=$(find "$report_dir" -name "index.html" -type f | head -n 1)
    
    if [ -z "$index_file" ]; then
        # If no index.html found in the main directory, check the html subdirectory
        index_file=$(find "$report_dir/html" -name "index.html" -type f 2>/dev/null | head -n 1)
        
        if [ -z "$index_file" ]; then
            echo -e "${YELLOW}No HTML report found. Would you like to generate one? (y/n)${NC}"
            read -r generate
            
            if [ "$generate" = "y" ] || [ "$generate" = "Y" ]; then
                generate_html_report "$report_dir"
                # After generation, try to find the index file again
                index_file=$(find "$report_dir/html" -name "index.html" -type f 2>/dev/null | head -n 1)
            else
                echo -e "${RED}No HTML report available.${NC}"
                exit 1
            fi
        fi
    fi
    
    if [ -n "$index_file" ]; then
        echo -e "${GREEN}Displaying HTML report from: $index_file${NC}"
        echo ""
        echo "-------------------- HTML REPORT CONTENT --------------------"
        cat "$index_file" | grep -v "</?style>" | grep -v -e "^$" | head -n 30
        echo "..."
        echo "-----------------------------------------------------------"
        echo -e "${YELLOW}Note: Showing truncated content. Full report available at:${NC}"
        echo "$index_file"
    fi
    
    # List other files in the directory
    echo ""
    echo -e "${BLUE}Available files in the report directory:${NC}"
    
    # JTL files
    local jtl_files=$(find "$report_dir" -name "*.jtl" -type f)
    if [ -n "$jtl_files" ]; then
        echo -e "${CYAN}JTL Files:${NC}"
        echo "$jtl_files" | while read -r file; do
            echo "  - $(basename "$file") ($(du -h "$file" | cut -f1))"
        done
    fi
    
    # Log files
    local log_files=$(find "$report_dir" -name "*.log" -type f)
    if [ -n "$log_files" ]; then
        echo -e "${CYAN}Log Files:${NC}"
        echo "$log_files" | while read -r file; do
            echo "  - $(basename "$file") ($(du -h "$file" | cut -f1))"
        done
    fi
}

# Generate or view reports
handle_reports() {
    local args=("$@")
    local view_path=""
    local generate_path=""
    local action=""
    
    # Parse arguments
    for ((i=0; i<${#args[@]}; i++)); do
        case ${args[i]} in
            "--view")
                action="view"
                if [ $((i+1)) -lt ${#args[@]} ] && [[ "${args[i+1]}" != --* ]]; then
                    view_path="${args[i+1]}"
                    i=$((i+1))
                fi
                ;;
            "--list")
                action="list"
                ;;
            "--generate")
                action="generate"
                ;;
            "--path="*)
                generate_path="${args[i]#*=}"
                ;;
        esac
    done
    
    # Execute appropriate action
    case $action in
        "view")
            if [ -z "$view_path" ]; then
                echo -e "${RED}Report path is required for view action.${NC}"
                print_help
                exit 1
            fi
            view_report "$view_path"
            ;;
        "list")
            list_reports
            ;;
        "generate")
            if [ -z "$generate_path" ]; then
                echo -e "${RED}Report directory path is required for generate action. Use --path=DIR${NC}"
                print_help
                exit 1
            fi
            generate_html_report "$generate_path"
            ;;
        *)
            echo -e "${RED}Unknown report action. Please specify --view, --list, or --generate${NC}"
            print_help
            exit 1
            ;;
    esac
}

# Clean target directory
clean_target() {
    echo -e "${BLUE}Cleaning target directory...${NC}"
    mvn clean
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Clean completed successfully!${NC}"
    else
        echo -e "${RED}Clean failed!${NC}"
        exit 1
    fi
}

# Main script execution
main() {
    print_banner
    
    # Check if a command is provided
    if [ $# -eq 0 ]; then
        print_help
        exit 1
    fi
    
    # Process commands
    case $1 in
        "setup")
            setup
            ;;
        "build")
            build_project
            ;;
        "run")
            if [ -z "$2" ]; then
                echo -e "${RED}Test type is required for run command.${NC}"
                print_help
                exit 1
            fi
            run_tests "$2"
            ;;
        "report")
            shift  # Remove 'report' from args
            if [ $# -eq 0 ]; then
                echo -e "${RED}Report action is required for report command.${NC}"
                print_help
                exit 1
            fi
            handle_reports "$@"
            ;;
        "clean")
            clean_target
            ;;
        "help")
            print_help
            ;;
        *)
            echo -e "${RED}Unknown command: $1${NC}"
            print_help
            exit 1
            ;;
    esac
}

# Execute main function with all arguments
main "$@"
