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

public class SaveTargetLog extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly assembly) throws MbException {
		//MbOutputTerminal out = getOutputTerminal("out");
		//MbOutputTerminal alt = getOutputTerminal("alternate");

		Connection conn = null;
		CallableStatement cs = null;
		
		try {
			MbMessage message = assembly.getMessage();
			// ----------------------------------------------------------
			// Add user code below
			MbElement body = message.getRootElement().getLastChild();
			conn = getJDBCType4Connection("{ESBPolicy}:ESBDBPolicy",
					JDBC_TransactionType.MB_TRANSACTION_AUTO);
			String messageId, transactionId, targetID, targetService, messageType, transactionDetail, messageTimestamp, iibMessageid, timeduration;
			messageId = transactionId = targetID = targetService = messageType = transactionDetail = messageTimestamp = iibMessageid = timeduration = null;
			messageId = getValue(body, "LogData/MessageID");
			transactionId = getValue(body, "LogData/TransID");
			targetID = getValue(body, "LogData/TargetID");
			targetService = getValue(body, "LogData/TargetService");		
			messageType = getValue(body, "LogData/MessageType");
			transactionDetail = getValue(body, "LogData/TransactionDetail");	
			messageTimestamp = getValue(body, "LogData/MessageTimestamp");
			iibMessageid = getValue(body, "LogData/IIBMessageID");
			timeduration = getValue(body, "LogData/TimeDuration");
			
			cs = conn.prepareCall("{call pkg_soaprocesslog.create_log_targetlog(?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, transactionId);
			cs.setString(2, messageId);
			cs.setString(3, iibMessageid);			
			cs.setString(4, targetID);
			cs.setString(5, targetService);	
			cs.setString(6, messageType);	
			cs.setObject(7, transactionDetail);			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
			if (messageTimestamp != null) {
				if (messageTimestamp.length() == 19){
				messageTimestamp += ".000";
				}
			}
			Date dateFromString = dateFormat.parse(messageTimestamp);
			Timestamp toTimestamp = new Timestamp(dateFromString.getTime());
			cs.setTimestamp(8, toTimestamp);
			cs.setString(9, timeduration);
			cs.execute();
			cs.close();
			// End of user code
			// ----------------------------------------------------------
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
		}
		finally {
			try {
				if (cs != null)
					cs.close();
			} catch (SQLException se2) {
				//se2.printStackTrace();
			}
		}
		// Change following to propagate the message to the 'out' or 'alt'
		// terminal
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
