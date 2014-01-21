package org.wso2.carbon.registry.extensions.handlers.utils;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class ConflictResolutionReader extends Reader {
    private static final Log log = LogFactory.getLog(ConflictResolutionReader.class);

    private static final String XPATH_EXPRESSION = "//lastModified";

    private Reader reader;
    private StringBuffer buffer;
    private String path;
    private Registry registry;

    public ConflictResolutionReader(Reader reader, String path, Registry registry) {
        this.reader = reader;
        this.path = path;
        this.registry = registry;
        buffer = new StringBuffer();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int retValue;

        retValue = reader.read(cbuf, off, len);
        if (retValue != -1) {
            buffer.append(cbuf);
        } else {
            checkForConflicts();
        }

        return retValue;
    }

    @Override
    public int read() throws IOException {
        int ret;

        ret = reader.read();
        if (ret != -1) {
            buffer.append((char) ret);
        } else {
            checkForConflicts();
        }

        return ret;
    }

    private void checkForConflicts() throws IOException {
        try {
            OMElement restoreElement = AXIOMUtil.stringToOM(buffer.toString());
            restoreElement.build();
            AXIOMXPath xpathQuery = new AXIOMXPath(XPATH_EXPRESSION);
            List lastModifiedTimes = xpathQuery.selectNodes(restoreElement);

            Collections.sort(lastModifiedTimes, new Comparator<OMElement>() {
                public int compare(OMElement o1, OMElement o2) {
                    long o1Value = Long.parseLong(o1.getText());
                    long o2Value = Long.parseLong(o2.getText());

                    if (o1Value < o2Value) {
                        return 1;
                    } else if (o1Value > o2Value) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            long maxTimeValue = getMaxTimeElement(lastModifiedTimes);

            LogEntry[] logs = registry.getLogs(null, LogEntry.ALL, null, new Date(maxTimeValue), null, true);

            Arrays.sort(logs, new Comparator<LogEntry>() {
                public int compare(LogEntry o1, LogEntry o2) {
                    return o2.getResourcePath().compareTo(o1.getResourcePath());
                }
            });

            for (LogEntry logEntry : logs) {
                if (logEntry.getResourcePath().startsWith(path)) {
                    final String msg = "Another user has modified the content of the resource " + path;
                    log.error(msg);
                    throw new IOException(msg);
                }
            }

        } catch (XMLStreamException e) {
            log.error("Error reading the restore file", e);
        } catch (JaxenException e) {
            log.error("Failed to initialize the Xpath", e);
        } catch (RegistryException e) {
            log.error("Unable to get logs from registry", e);
        }
    }

    /*
        Checking to see whether the version field is 0
        This is done to identify the newly added resources
    */
    private long getMaxTimeElement(List lastUpdateTimes) {
        for (Object lastUpdateTime : lastUpdateTimes) {
            OMElement maxTimeElement = (OMElement) lastUpdateTime;
            OMContainer parentElement = maxTimeElement.getParent();
            OMElement versionChild;
            if ((versionChild = parentElement.getFirstChildWithName(new QName("version"))) != null) {
                if (Integer.parseInt(versionChild.getText()) > 0) {
                    return Long.parseLong(maxTimeElement.getText());
                }
            }
        }
        return Long.MIN_VALUE;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }
}
