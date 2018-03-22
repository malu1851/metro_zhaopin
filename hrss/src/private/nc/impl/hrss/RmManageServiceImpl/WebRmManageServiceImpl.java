package nc.impl.hrss.RmManageServiceImpl;


import nc.bs.dao.BaseDAO;
import nc.bs.hrss.RmManageService.WebRmManageService;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.vo.pub.BusinessException;

public class WebRmManageServiceImpl implements  WebRmManageService{

	@Override
	public Object[] RmResume(String key) throws BusinessException {
		
		StringBuffer strBuffer = new StringBuffer();

		strBuffer.append("select applystatus  from rm_psndoc_job  where rm_psndoc_job.pk_psndoc = '"+ key +"' ");

		BaseDAO dao = new BaseDAO();

		Object[] rmStatus = (Object[]) dao.executeQuery(
				strBuffer.toString(), new ArrayProcessor());

		return rmStatus;

	}

	@Override
	public Object[]  CountRmResume(String key) throws BusinessException {

		StringBuffer strBuffer = new StringBuffer();

		strBuffer.append("select count(*)  from rm_psndoc_job  where rm_psndoc_job.pk_psndoc = '"+ key +"' ");

		BaseDAO dao = new BaseDAO();

		Object[]   countResume =  (Object[]) dao.executeQuery (strBuffer.toString(), new ArrayProcessor());

		return countResume;
	}

	@Override
	public Object[] RmStatu(String psndoc) throws BusinessException {
		
		StringBuffer strBuffer = new StringBuffer();

		strBuffer.append("select applystatus  from rm_psndoc_job  where rm_psndoc_job.pk_psndoc = '"+ psndoc +"' order by ts desc  ");

		BaseDAO dao = new BaseDAO();

		Object[]   rmStatus =  (Object[]) dao.executeQuery (strBuffer.toString(), new ArrayProcessor());

		return rmStatus;
	}


	

}
