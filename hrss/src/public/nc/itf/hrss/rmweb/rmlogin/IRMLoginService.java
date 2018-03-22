package nc.itf.hrss.rmweb.rmlogin;

import nc.bs.dao.DAOException;
import nc.vo.hrss.pub.rmweb.RmUserVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;

public abstract interface IRMLoginService {
	public abstract String insertVO(RmUserVO paramRmUserVO) throws DAOException;

	public abstract RmUserVO loginRMWeb(String paramString1, String paramString2)
			throws BusinessException;

	public abstract UFDouble checkEMail(String paramString)
			throws BusinessException;

	public abstract UFDouble checkIdCard(String paramString)
			throws BusinessException;

	public abstract int updateVO(RmUserVO paramRmUserVO) throws DAOException;

	public abstract UFDouble checkHrrmpsndoc(String paramString1,
			String paramString2) throws BusinessException;

	public abstract UFDouble checkPsndocJob(String paramString)
			throws BusinessException;

	public abstract OrgVO checkJobOrg(String paramString)
			throws BusinessException;

	public abstract RmUserVO qryRmUserVOByEmail(String paramString)
			throws BusinessException;

	public abstract RmUserVO qryRmUserVOByIdcard(String paramString)
			throws BusinessException;
}