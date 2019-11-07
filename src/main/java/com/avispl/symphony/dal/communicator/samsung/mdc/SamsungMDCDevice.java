package com.avispl.symphony.dal.communicator.samsung.mdc;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.monitor.Monitorable;

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
            if(controllableProperty.getValue().toString().equals(powerStatusNames.ON.name())){
                powerON();
            }else if(controllableProperty.getValue().toString().equals(powerStatusNames.OFF.name())){
                powerOFF();
            }
        }else if (controllableProperty.getProperty().equals(commandNames.INPUT.name())){
            setInput(inputNames.valueOf(controllableProperty.getValue().toString()));
        }
    }

    @Override
    public void controlProperties(List<ControllableProperty> list) throws Exception {

    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        ExtendedStatistics extendedStatistics = new ExtendedStatistics();

        Map<String, String> controllable = new HashMap<String, String>(){{
            put(commandNames.POWER.name(),powerStatus.keySet().toString());
            put(commandNames.INPUT.name(),inputs.keySet().toString());
        }};

        Map<String, String> statistics = new HashMap<String, String>();

        try {
            statistics.put(commandNames.POWER.name(), getPower().name());
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getPower", e);
            }
            throw e;
        }

        SamsungMDCStatus status = getStatus();

        try {
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

        try {
            statistics.put(commandNames.INPUT.name(), getInput().name());
        }catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during getInput", e);
            }
            throw e;
        }

        extendedStatistics.setControl(controllable);
        extendedStatistics.setStatistics(statistics);

        return Collections.singletonList(extendedStatistics);
    }

    private powerStatusNames getPower() throws Exception{

        String response = send(SamsungMDCUtils.buildSendString((char)monitorID,commands.get(commandNames.POWER)));

        powerStatusNames power= (powerStatusNames)digestResponse(response,commandNames.POWER);

        if(power == null)
        {
            throw new Exception();
        }else{
            return power;
        }
    }

    private void powerON(){
        String toSend = SamsungMDCUtils.buildSendString((char)monitorID,commands.get(commandNames.POWER),Character.toString(powerStatus.get(powerStatusNames.ON)));

        try {
            String response = send(toSend);

            digestResponse(response,commandNames.POWER);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power ON send", e);
            }
        }
    }

    private void powerOFF(){
        String toSend = SamsungMDCUtils.buildSendString((char)monitorID,commands.get(commandNames.POWER),Character.toString(powerStatus.get(powerStatusNames.OFF)));

        try {
            String response = send(toSend);

            digestResponse(response,commandNames.POWER);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during power OFF send", e);
            }
        }
    }

    private inputNames getInput()throws  Exception{
        String response = send(SamsungMDCUtils.buildSendString((char)monitorID,commands.get(commandNames.INPUT)));

        inputNames input = (inputNames)digestResponse(response,commandNames.INPUT);

        if(input == null)
        {
            throw new Exception();
        }else{
            return input;
        }
    }

    private void setInput(inputNames i){
        String toSend = SamsungMDCUtils.buildSendString((char)monitorID,commands.get(commandNames.INPUT),Character.toString(inputs.get(i)));

        try {
            String response = send(toSend);

            digestResponse(response,commandNames.INPUT);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("error during setInput send", e);
            }
        }
    }

    private SamsungMDCStatus getStatus() throws  Exception{
        String response = send(SamsungMDCUtils.buildSendString((char)monitorID,commands.get(commandNames.STATUS)));

        SamsungMDCStatus status = (SamsungMDCStatus)digestResponse(response,commandNames.STATUS);

        if(status == null)
        {
            throw new Exception();
        }else {
            return status;
        }
    }

    private Object digestResponse(String response, commandNames expectedResponse){
        Set set = null;

        if(SamsungMDCUtils.checkSum(response.substring(1,response.length()-1).getBytes()) == (byte)response.charAt(response.length()-1)){
            switch (response.charAt(4)){
                case 'A':
                {
                    switch (expectedResponse){
                        case POWER:{
                            if(response.charAt(5) == commands.get(commandNames.POWER)){
                                set = powerStatus.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((char)mentry.getValue() == response.charAt(6)){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }case INPUT:{
                            if(response.charAt(5) == commands.get(commandNames.INPUT)){
                                set = inputs.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((char)mentry.getValue() == response.charAt(6)){
                                        return (Enum)mentry.getKey();
                                    }
                                }
                            }else{
                                throw new RuntimeException("Unexpected response");
                            }
                            break;
                        }case STATUS:{
                            if(response.charAt(5) == commands.get(commandNames.STATUS)){

                                return new SamsungMDCStatus(statusCodes.get(response.charAt(6)),
                                        statusCodes.get(response.charAt(7)),
                                        statusCodes.get(response.charAt(8)),
                                        statusCodes.get(response.charAt(9)),
                                        statusCodes.get(response.charAt(11)),response.charAt(10));
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
                            if(response.charAt(5) == commands.get(commandNames.INPUT)){
                                set = inputs.entrySet();

                                Iterator iterator = set.iterator();
                                while(iterator.hasNext()) {
                                    Map.Entry mentry = (Map.Entry)iterator.next();

                                    if((char)mentry.getValue() == response.charAt(6)){
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
