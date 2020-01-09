package com.avispl.symphony.dal.communicator.samsung.mdc;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;

import java.io.IOException;
import java.util.*;

import static com.avispl.symphony.dal.communicator.samsung.mdc.SamsungMDCConstants.*;

public class SamsungMDCDevice extends SocketCommunicator implements Controller, Monitorable {

    private int monitorID;

    public SamsungMDCDevice(){
        super();

        this.setPort(1515);
        this.monitorID = 1;

        // set list of command success strings (included at the end of response when command succeeds, typically ending with command prompt)
        this.setCommandSuccessList(Collections.singletonList("A"));
        // set list of error response strings (included at the end of response when command fails, typically ending with command prompt)
        this.setCommandErrorList(Collections.singletonList("ERROR"));
    }

    public int getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(int monitorID) {
        this.monitorID = monitorID;
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {

        if (controllableProperty.getProperty().equals(commandNames.POWER.name())){
            if(controllableProperty.getValue().toString().equals("1")){
                powerON();
            }else if(controllableProperty.getValue().toString().equals("0")){
                powerOFF();
            }
        }
    }

    @Override
    public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
        // same as controlProperty(ControllableProperty controllableProperty), but for a multiples of ControllableProperty
        controllableProperties.stream().forEach(p -> {
            try {
                controlProperty(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        ExtendedStatistics extendedStatistics = new ExtendedStatistics();

        Map<String, String> controllable = new HashMap<String, String>(){{
            put(commandNames.POWER.name(),"Toggle");
        }};

        Map<String, String> statistics = new HashMap<String, String>();

        String power;

        try {
            power = getPower().name();
            if(power.compareTo("ON") == 0) {
                statistics.put(commandNames.POWER.name(), "1");
            }else if(power.compareTo("OFF") == 0)
            {
                statistics.put(commandNames.POWER.name(), "0");
            }
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getPower", e);
            }
            throw e;
        }



        try {
            SamsungMDCStatus status = getStatus();

            statistics.put(statusNames.LAMP.name(), status.getLamp().name());
            statistics.put(statusNames.TEMPERATURE_CODE.name(), status.getTemperatureError().name());
            statistics.put(statusNames.BRIGHTNESS_SENSOR.name(), status.getBrightnessSensor().name());
            statistics.put(statusNames.NO_SYNC.name(), status.getNoSync().name());
            statistics.put(statusNames.FAN.name(), status.getFan().name());
            statistics.put(statusNames.TEMPERATURE.name(), Integer.toString(status.getTemperature()));
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getStatus", e);
            }
            throw e;
        }

        String input;

        try {
            input =  getInput().name();
            statistics.put(commandNames.INPUT.name(), input);
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getInput", e);
            }
            throw e;
        }

        extendedStatistics.setControl(controllable);
        extendedStatistics.setStatistics(statistics);

        if(this.logger.isDebugEnabled()) {
            for (String s : controllable.keySet()) {
                this.logger.debug("controllable key: " + s + ",value: " + controllable.get(s));
            }
        }

        if(this.logger.isDebugEnabled()) {
            for (String s : statistics.keySet()) {
                this.logger.debug("statistics key: " + s + ",value: " + statistics.get(s));
            }
        }

        //return Collections.singletonList(extendedStatistics);
        return new ArrayList<Statistics>(Collections.singleton(extendedStatistics));
    }

    private powerStatusNames getPower() throws Exception{

        byte[] response = send(SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.POWER)));

        powerStatusNames power= (powerStatusNames)digestResponse(response,commandNames.POWER);

        if(power == null)
        {
            throw new Exception();
        }else{
            return power;
        }
    }

    private void powerON() throws IOException {

        byte[] toSend = SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.POWER),new byte[]{powerStatus.get(powerStatusNames.ON)});

        try {
            byte[] response = send(toSend);

            digestResponse(response,commandNames.POWER);


            destroyChannel();
            synchronized(this) {//synchronized block
                Thread.sleep(20000);
            }
            //wait(10000);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power ON send", e);
            }
        }
    }

    private void powerOFF() throws IOException {

        byte[] toSend = SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.POWER),new byte[]{powerStatus.get(powerStatusNames.OFF)});

        try {
            byte[] response = send(toSend);

            digestResponse(response,commandNames.POWER);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power OFF send", e);
            }
        }
    }

    private inputNames getInput()throws  Exception {
            byte[] response = send(SamsungMDCUtils.buildSendString((byte) monitorID, commands.get(commandNames.INPUT)));

            inputNames input = (inputNames) digestResponse(response, commandNames.INPUT);

            if(input == null)
            {
                throw new Exception();
            }else{
                return input;
            }
    }

    private void setInput(inputNames i){
        byte[] toSend = SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.INPUT),new byte[]{inputs.get(i)});

        try {
            byte[] response = send(toSend);

            digestResponse(response,commandNames.INPUT);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during setInput send", e);
            }
        }
    }

    private SamsungMDCStatus getStatus() throws  Exception{
        byte[] response = send(SamsungMDCUtils.buildSendString((byte)monitorID,commands.get(commandNames.STATUS)));

        SamsungMDCStatus status = (SamsungMDCStatus)digestResponse(response,commandNames.STATUS);

        if(status == null)
        {
            throw new Exception();
        }else {
            return status;
        }
    }

    private Object digestResponse(byte[] responseBytes, commandNames expectedResponse){
        Set set = null;

        byte checksumByte = SamsungMDCUtils.checkSum(java.util.Arrays.copyOfRange(responseBytes,1,responseBytes.length-1));

        if(checksumByte == responseBytes[responseBytes.length-1]){
            switch (responseBytes[4]){
                case 'A':
                {
                    switch (expectedResponse){
                        case POWER:{
                            if(responseBytes[5] == commands.get(commandNames.POWER)){
                                set = powerStatus.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((byte)mentry.getValue() == responseBytes[6]){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }case INPUT:{
                            if(responseBytes[5] == commands.get(commandNames.INPUT)){
                                set = inputs.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((byte)mentry.getValue() == responseBytes[6]){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }case STATUS:{
                            if(responseBytes[5] == commands.get(commandNames.STATUS)){
                                return new SamsungMDCStatus(statusCodes.get(responseBytes[6]),
                                        statusCodes.get(responseBytes[7]),
                                        statusCodes.get(responseBytes[8]),
                                        statusCodes.get(responseBytes[9]),
                                        statusCodes.get(responseBytes[11]),responseBytes[10]);
                            }else{
                                throw new RuntimeException("Unexpected response");
                            }
                        }
                    }
                    break;
                }
                case 'N':
                {
                    switch (expectedResponse){
                        case POWER:{
                            throw new RuntimeException("Power command returned NAK");
                        }case INPUT:{
                            if(responseBytes[5] == commands.get(commandNames.INPUT)){
                                set = inputs.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((byte)mentry.getValue() == responseBytes[6]){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }
                    }
                    break;
                } default:
                {
                    throw new RuntimeException("Nor ACK or NAK received");
                }
            }
        }else{
            throw new RuntimeException("wrong Checksum received");
        }
        throw new RuntimeException("End of digestResponse reached without a solution");
    }
}
