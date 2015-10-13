package test;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;

import oracle.bpm.client.BPMServiceClientFactory;
import oracle.bpm.services.common.exception.BPMException;
import oracle.bpm.services.instancequery.IInstanceQueryService;
import oracle.bpm.ui.Image;
import oracle.bpm.ui.utils.ImageExtension;
import oracle.bpm.ui.utils.ImageIOFacade;

public class AuditProcessImage {
    public AuditProcessImage() {
        super();
    }

    /**
     * get BPMServiceClientFactory from property file
     * @return BPMServiceClientFactory
     */
    public static BPMServiceClientFactory getBPMServiceClientFactory() {

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(new File("connection.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String soaURL = "t3://" + prop.getProperty("hostname") + ":" + prop.getProperty("port");

        Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> properties;
        properties = new EnumMap<>(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.class);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.CLIENT_TYPE,
                       WorkflowServiceClientFactory.REMOTE_CLIENT);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, soaURL);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_PRINCIPAL,
                       prop.getProperty("username"));
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_CREDENTIALS,
                       prop.getProperty("password"));
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_INITIAL_CONTEXT_FACTORY,
                       "weblogic.jndi.WLInitialContextFactory");
        return BPMServiceClientFactory.getInstance(properties, null, null);
    }

    static enum IMAGE_TYPE {
        PROCESS,
        AUDIT,
    };

    /**
     * Main method for test application.
     * @param args[0] Instance Id [1] Full Path for output image file [2] Image Type (Audit/Process)
     */
    public static void main(String[] args) {
        // Argument check
        // [0] Instance ID [1] Output File Path (In case of Windows, seperator of "\\" is required) [2] Audit|Process
        if (args.length != 3) {
            System.err.println("java auditimage.InstanceImage {Instance ID} {full path of output image file} {Audit|Process}");
            System.exit(1);
        }
        String instanceId = args[0];
        String outputPath = args[1];
        IMAGE_TYPE imageType;
        // File output
        if (args[2].equalsIgnoreCase("Process")) {
            imageType = IMAGE_TYPE.PROCESS;
        } else if (args[2].equalsIgnoreCase("Audit")) {
            imageType = IMAGE_TYPE.AUDIT;
        } else {
            imageType = null;
            System.err.println("Specify \"Audit\", or \"Process\" as 3rd argument.");
            System.exit(1);
        }

        // Connect to BPM Server
        // get BPMContext and create Utility Class instance
        // Locale is fixed to ja_JP in this sample.
        BPMServiceClientFactory bpmServiceClientFactory = getBPMServiceClientFactory();
        IBPMContext bpmContext;
        String Base64 = null;
        try {
            bpmContext = bpmServiceClientFactory.getBPMUserAuthenticationService().getBPMContextForAuthenticatedUser();
            IInstanceQueryService instanceQueryService =
                bpmServiceClientFactory.getBPMServiceClient().getInstanceQueryService();
            if (imageType.equals(IMAGE_TYPE.PROCESS)) {
                Base64 = instanceQueryService.getProcessDiagram(bpmContext, instanceId, Locale.JAPAN);
            } else if (imageType.equals(IMAGE_TYPE.AUDIT)) {
                Base64 = instanceQueryService.getProcessAuditDiagram(bpmContext, instanceId, Locale.JAPAN);
            }
        } catch (BPMException e) {
            e.printStackTrace();
        }

        try {
            Image image = Image.createFromBase64(Base64);
            BufferedImage bufferedImage = (BufferedImage) image.asAwtImage();

            try (ByteArrayOutputStream auditImageOutputStream = new ByteArrayOutputStream();) {
                ImageIOFacade.writeImage(bufferedImage, ImageExtension.PNG, auditImageOutputStream);
                try (InputStream diagram = new ByteArrayInputStream(auditImageOutputStream.toByteArray())) {
                    writeToFile(diagram, outputPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(InputStream istream, String outputFilePath) throws IOException {
        try (OutputStream out = new FileOutputStream(outputFilePath)) {
            // output file
            byte[] buf = new byte[1024];
            int len;
            while ((len = istream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
