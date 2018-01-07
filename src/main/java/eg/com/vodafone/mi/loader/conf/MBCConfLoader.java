package eg.com.vodafone.mi.loader.conf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eg.com.vodafone.mi.loader.IResultSetHandler;
import eg.com.vodafone.mi.loader.JDBCTemplate;

public class MBCConfLoader implements IBackendConfLoader
{
    private static Logger logger = LoggerFactory.getLogger(MBCConfLoader.class);

    private JDBCTemplate jdbcTemplate;

    public MBCConfLoader(JDBCTemplate jdbcTemplate)
    {
	this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getType()
    {
	return "PREPAID";
    }

    @Override
    public Map<String, Map<String, String>> loadConf(final String externalID)
    {
	final Map<String, Map<String, String>> services = new HashMap<String, Map<String, String>>();

	this.jdbcTemplate.query(
		"SELECT OPERATION, SUBJECT, SUBJECT_PARSER, EXTERNAL_SRV_ID FROM TIBPRD_SRV_OPERATION WHERE OPERATION_SRV_ID = '"
			+ externalID + "' AND PRD_STREAM = 'MI' ", new IResultSetHandler()
		{
		    @Override
		    public void handle(ResultSet resultSet)
		    {
			try
			{
			    if (resultSet.next())
			    {
				Map<String, String> service = new HashMap<String, String>();
				service.put("OPERATION", resultSet.getString("OPERATION"));
				service.put("SUBJECT", resultSet.getString("SUBJECT"));
				service.put("SUBJECT_PARSER", resultSet.getString("SUBJECT_PARSER"));
				service.put("EXTERNAL_SRV_ID", resultSet.getString("EXTERNAL_SRV_ID"));

				service.putAll(queryPrepaidTable(resultSet.getString("EXTERNAL_SRV_ID")));
				services.put(externalID, service);
			    }
			}
			catch (SQLException e)
			{
			    logger.error("Exception: ", e);
			}
		    }
		});

	return services;
    }

    private Map<String, String> queryPrepaidTable(String externalID)
    {
	final Map<String, String> service = new HashMap<String, String>();

	this.jdbcTemplate.query("SELECT SERVICE, BUNDLE_ID FROM TIBSERV_PREPAID WHERE PREPAID_SERV_ID = '" + externalID
		+ "' AND PRD_STREAM = 'MI' ", new IResultSetHandler()
	{
	    @Override
	    public void handle(ResultSet resultSet)
	    {
		try
		{
		    if (resultSet.next())
		    {
			service.put("SERVICE", resultSet.getString("SERVICE"));
			service.put("BUNDLE_ID", resultSet.getString("BUNDLE_ID"));
		    }
		}
		catch (SQLException e)
		{
		    logger.error("Exception: ", e);
		}
	    }
	});

	return service;
    }

}