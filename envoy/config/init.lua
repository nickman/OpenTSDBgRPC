DEBUG = false

-------------------------------------
-- Logs formatted output
-- @param fmt The log format expression
-- @param ... The vargs to fill in the expression
-------------------------------------
local function log(fmt, ...)
  print(string.format(fmt, unpack({...})))
end

-------------------------------------
-- Logs formatted output if DEBUG is true
-- @param fmt The log format expression
-- @param ... The vargs to fill in the expression
-------------------------------------
local function debug(fmt, ...)
  if DEBUG then
    log("DEBUG:"..fmt, ...)
  end
end

-------------------------------------
-- Executes the specified command and captures the output
-- @param command The OS command template to execute
-- @param ... vargs to fill the command template
-- @return The output of the command
-------------------------------------
local function osExecAndRead(command, ...)
  local fname = os.tmpname()
  local cmd = string.format(command, unpack({...})).." > "..fname
  debug("osExecAndRead(%s)", cmd)
  os.execute(cmd)
  local file = io.open(fname, "r");  
  local content = file:read "*a"
  file:close()
  os.remove(fname)
  return content:gsub("\n", "")
end



-------------------------------------
-- Returns the template for the consul registration json
-- @return the template
-------------------------------------
local function template()
  return [[
    {
      "ID": "%s",
      "Name": "envoy",
      "Tags": [
        "primary",
        "v2",
        "envoy",
        "pid=%s"
      ],
      "Address": "%s",
      "Port": %s,
      "Check": {
        "Name" : "Envoy Up Check",
        "DeregisterCriticalServiceAfter": "1m",
        "HTTP": "http://%s:%s/server_info",
        "Method": "GET",
        "Interval": "3s",
        "TLSSkipVerify": true
      }
    }
  ]]
end

-------------------------------------
-- Returns the PID of the envoy process
-- @return the PID
-------------------------------------
local function getPid()
  local file = io.open("/proc/self/stat", "r");  
  local content = file:read "*a"
  local words = {}
  for word in content:gmatch("%w+") do table.insert(words, word) end
  file:close()
  _G["PID"] = words[1]
  return words[1]
end

-------------------------------------
-- Prints the contents of the passed table to stdout
-- For debugging only.
-- @param name The name of the table
-- @param tab The table to print
-------------------------------------
local function printTable(name, tab) 
  io.write(string.format("\n================\nTABLE: %s\n================\n", name))
  for k,v in pairs(tab)  do
    io.write(string.format("\n\tK: %s, V: %s", k,v))
  end
  io.write("\n")
  io.flush()
end

-------------------------------------
-- Trims leading and trailing whitespace from the passed string
-- @param s The string to trim
-- @return The trimmed string
-------------------------------------
function trim(s)
  return (s:gsub("^%s*(.-)%s*$", "%1"))
end

-------------------------------------
-- Determines if the passed string starts with the passed prefix
-- @param s The string to test
-- @param prefix The prefix to test for
-- @return true if the string starts with the prefix, false otherwise
-------------------------------------
function startsWith(s, prefix)
   return string.sub(s,1,string.len(prefix))==prefix
end

-------------------------------------
-- Determines if the passed string contains the passed match
-- @param s The string to test
-- @param match The match to test for
-- @return true if the string contains the match, false otherwise
-------------------------------------
function contains(s, match)
   return string.match(s, match)
end

-------------------------------------
-- Extracts the admin port from the envoy configuration file
-- @param confFile The name of the file
-- @return The admin port or -1 if not found
-------------------------------------
local function getAdminPort(confFile) 
  debug("getAdminPort(%s)", confFile)
  local inAdmin = false
  local port = -1
  for line in io.lines(confFile) do
    local s = trim(line)
    if s == "admin:" then
      inAdmin = true
    end
    if inAdmin then
      if contains(s, "port_value:") then
        local p = string.match(s, "port_value:%s*(%d+)")
        port = tonumber(p);
        break
      end
    end
  end
  return port
end

-------------------------------------
-- Extracts the listener port from the envoy configuration file
-- @param confFile The name of the file
-- @return The listener port or -1 if not found
-------------------------------------
local function getListenerPort(confFile) 
  debug("getListenerPort(%s)", confFile)
  local inListener = false
  local port = -1
  for line in io.lines(confFile) do
    local s = trim(line)
    if s == "listeners:" then
      inListener = true
      debug("Found listeners")
    end
    if inListener then
      if contains(s, "port_value:") then
        debug("Found Listeners port_value")
        local p = string.match(s, "port_value:%s*(%d+)")
        port = tonumber(p);
        debug("Listeners port: %s", port)
        break
      end
    end    
  end
  return port
end

-------------------------------------
-- Returns a lua table command containing the line argument values for the envoy process
-- indexed by the config key with out leading dashes. 
-- e.g. if the command line arguments contained:
-- "--config-path xxx"
-- then the returned table will contain table["config-path"]="xxx"
-- The table is also registered as a global table "CMDLINE".
-- @param pid The pid of the envoy process
-- @return A lua table of command line values keyed by the config key
-------------------------------------
local function loadCmdLine(pid)
  debug("loadCmdLine(%s)", pid)
  local fname = os.tmpname()  
  os.execute(string.format("cat /proc/%s/cmdline | xargs -0 echo > %s", pid, fname))
  local file = io.open(fname, "r");  
  local content = file:read "*a"
  file:close()
  os.remove(fname)
  local args = {}
  local key = nil
  for i in string.gmatch(content, "%S+") do
    if startsWith(i, "-") then
      key = i;
      while startsWith(key, "-") and string.len(key) > 0 do
        key = string.sub(key, 2)
      end
    else
      if key ~= nil then
        args[key] = i
        key = nil
      end
    end
  end
  _G["CMDLINE"] = args
  return args
end

-------------------------------------
-- Determines if the named file exists
-- @param name The name of the file to test for
-- @return true if the file exists, false otherwise
-------------------------------------
local function file_exists(name)
  local f=io.open(name,"r")
  if f~=nil then io.close(f) return true else return false end
end

-------------------------------------
-- Writes the passed string to a new temp file
-- @param s The string to write
-- @return The name of the file writen to
-------------------------------------
local function writeJson(json)
  debug("writeJson(...)")
  local fname = os.tmpname()
  local f = assert(io.open(fname, "w"))
  f:write(json)
  f:close()
  return fname
end


-------------------------------------
-- Registers this envoy process in consul
-- @param s The varg values to fill the json template tokens
-------------------------------------
local function register(...) 
  debug("register(...)")
  local template = template();
  local args = {...}
  local postJson = string.format(template, unpack(args))

  debug("Service Registration JSON:\n====\n%s\n====", postJson)
  
  local fname = writeJson(postJson)
  local consulHost = os.getenv("CONSUL_HOST")
  if consulHost == nil then
    consulHost = "localhost"
  end
  if consulPort == nil then
    consulPort = "8500"
  end
  local consulPort = os.getenv("CONSUL_PORT")
  local putCmd = string.format("curl --request PUT --data @"..fname.." http://%s:%s/v1/agent/service/register", 
    consulHost, consulPort)
  debug("Consul Put Command: %s", putCmd)
  local posted = os.execute(putCmd)  
  os.remove(fname)
  if posted then
    print("Envoy instance registered in consul")
  else
    print("Envoy instance registration failed. Exiting ....")
    os.exit()
  end
end


-------------------------------------
-- Finds the value of a command line argument by the command key
-- without the leading dashes.
-- Most options only have one key, but ones like config file
-- have two, so this function accepts a varg and will return
-- the first match.
-- @param s One or more command line keys.
-------------------------------------
local function cmdLineArg(...)
  debug("cmdLineArg(%s)", ...)
  local t = {...}
  local value = nil
  for _,v in pairs(t) do
    value = _G["CMDLINE"][v]
    if value ~= nil then
      break
    end
  end
  return value
end


-------------------------------------
-- The entry point to read the config
-- and register the envoy instance in Consul
-------------------------------------
function init()
  local TMPDIR = osExecAndRead("echo $(dirname $(mktemp -u))")
  local PID = getPid()          
  local fname = string.format("/%s/%s.envoy", TMPDIR, PID)
  if file_exists(fname) ~= true then
    local f=io.open(fname,"w+")
    f:write(PID)
    f:close()
    
    loadCmdLine(PID)
    local configFile = cmdLineArg("c", "config-path")
    debug("CONFIG FILE: %s", configFile)
    local serviceZone = cmdLineArg("service-zone")
    debug("SERVICE ZONE: %s", serviceZone)
    local serviceNode = cmdLineArg("service-node")
    debug("SERVICE NODE: %s", serviceNode)
    local serviceCluster = cmdLineArg("service-cluster")
    debug("SERVICE CLUSTER: %s", serviceCluster)
    local aport = getAdminPort(configFile)
    debug("ADMIN PORT: %s", aport)
    local port = getListenerPort(configFile)
    debug("LISTENER PORT: %s", port)
    local host = osExecAndRead("hostname")
    debug("HOST: %s", host)
    local addr = cmdLineArg("admin-address-path")
    if addr == nil then
      addr = host
    end
    debug("ADMIN ADDR: %s", addr)
    
    debug("CONFIG:\n\tconfig-file:%s\n\tzone:%s\n\tnode:%s\n\tcluster:%s\n\tadmin-port:%s\n\tadmin-addr:%s", configFile, serviceZone, serviceNode, serviceCluster, aport, addr)
    -- Args are: Service ID, PID, Address, Port, Address, Port
    register(host.."-envoy-"..aport, PID, addr, port, addr, aport)
    log("\n\t------------------------------\n\tINITIALIZED: PID=%s\n\t------------------------------\n", PID)
  end
end

-- Starts the registration
init()


