package soa.log;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbUserException;

public class SaveException extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly assembly) throws MbException {
		//MbOutputTerminal out = getOutputTerminal("out");
		//MbOutputTerminal alt = getOutputTerminal("alternate");
		Connection conn = null;
		CallableStatement cs = null;
		try {
			MbMessage message = assembly.getMessage();
			MbElement body = message.getRootElement().getLastChild();
			conn = getJDBCType4Connection("{ESBPolicy}:ESBDBPolicy",
					JDBC_TransactionType.MB_TRANSACTION_AUTO);
			String messageId, transactionId, messageTimestamp, serviceName, operationName, serviceVersion, hostName, nodeName, flowName, clientIP, errorDetail, errorCode, errorMessage, data;
			messageId = transactionId = messageTimestamp = serviceName = operationName = serviceVersion = hostName = clientIP = errorCode = errorMessage = data = null;
			messageId = getValue(body, "Exception/MessageId");
			transactionId = getValue(body, "Exception/TransactionId");
			messageTimestamp = getValue(body, "Exception/MessageTimeStamp");
			serviceName = getValue(body, "Exception/ServiceName");
			operationName = getValue(body, "Exception/OperationName");
			serviceVersion = getValue(body, "Exception/ServiceVersion");
			hostName = getValue(body, "Exception/HostName");
			nodeName = getValue(body, "Exception/NodeName");
			flowName = getValue(body, "Exception/FLowName");
			errorMessage = getValue(body, "Exception/ErrorMsg");
			errorCode = getValue(body, "Exception/ErrorCode");
			errorDetail = getValue(body, "Exception/ErrorDetails");
			data = getValue(body, "Exception/Data");
			clientIP = getValue(body, "Exception/ClientIP");

			cs = conn
					.prepareCall("{call pkg_soaprocesslog.CREATE_LOG_SOAEXCEPTION(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			// String sql = "CALL pkg_common.getdata_from_user_service('ALL')";
			cs.setString(1, messageId);
			cs.setString(2, transactionId);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
			if (messageTimestamp != null) {
				if (messageTimestamp.length() == 19){
				messageTimestamp += ".000";
				}
			}
			Date dateFromString = dateFormat.parse(messageTimestamp);
			Timestamp toTimestamp = new Timestamp(dateFromString.getTime());
			cs.setTimestamp(3, toTimestamp);
			cs.setString(4, serviceName);			
			cs.setString(5, operationName);
			cs.setString(6, serviceVersion);
			cs.setString(7, hostName);
			cs.setString(8, nodeName);
			cs.setString(9, flowName);
			cs.setString(10, errorMessage);
			cs.setString(11, errorCode);
			cs.setObject(12, errorDetail);
			cs.setObject(13, data);
			cs.setString(14, clientIP);
			cs.execute();
			cs.close();
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}finally {
			try {
				if (cs != null)
					cs.close();
			} catch (SQLException se2) {
				//se2.printStackTrace();
			}
		}

		// Change following to propagate the message to the 'out' or 'alt' terminal
		//out.propagate(assembly);
	}
	
	private String getValue(MbElement body, String path) {
		String result = null;
		try {
			if (body.getFirstElementByPath(path) != null) {
				result = body.getFirstElementByPath(path).getValueAsString();
			}
		} catch (MbException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return result;
	}

}
