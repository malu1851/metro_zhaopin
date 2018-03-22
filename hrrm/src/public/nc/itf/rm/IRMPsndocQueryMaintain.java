package nc.itf.rm;

import java.util.Map;

import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.vo.hi.psndoc.PsnJobVO;
import nc.vo.hr.tools.pub.GeneralVO;
import nc.vo.pub.BusinessException;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.RMPsnCPVO;
import nc.vo.rm.psndoc.RMPsndocVO;
import nc.vo.uif2.LoginContext;

/**
 * ӦƸ��Ա��ѯ�ӿ�
 * @author yucheng
 *
 */
public interface IRMPsndocQueryMaintain {

	/**
	 * ͨ��������ѯӦƸ��Ա
	 * @param context
	 * @param fromWhereSQL
	 * @param psnType ���ͣ�ӦƸ�Ǽǡ���ѡͨ���������С�¼���С�������
	 * @return
	 * @throws BusinessException
	 */
	public Map<Integer, Object[]> queryApplyPsns(LoginContext context, Map<Integer, FromWhereSQL> map) throws BusinessException;
	/**
	 * ͨ��������ѯӦƸ��Ա����������Ա��ͼƬ��
	 * @param context
	 * @param map
	 * @return
	 * @throws BusinessException
	 */
	public Map<Integer, Object[]> queryApplyPsnFiltPhotos(LoginContext context, Map<Integer, FromWhereSQL> map)throws BusinessException;
	
	/**
	 * ��ѯָ����Ա�Ƽ���ӦƸ��Ա��Ϣ
	 * @param pk_psndoc
	 * @param fromWhereSQL
	 * @return
	 * @throws BusinessException
	 */
	public AggRMPsndocVO[] queryRecommendPsns(String pk_psndoc, FromWhereSQL fromWhereSQL) throws BusinessException;
	
	/**
	 * ͨ��������ѯӦƸ��Ա��Ϣ
	 * @param pk_psndoc
	 * @return
	 * @throws BusinessException
	 */
	public AggRMPsndocVO queryByPK(String pk_psndoc) throws BusinessException;
	
	public AggRMPsndocVO queryByPK(String pk_org ,String pk_psndoc) throws BusinessException;
	
	/**
	 * ��ѯ��Ƹ��Ա
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
	public AggRMPsndocVO[] queryReApplyPsns(String pk_org) throws BusinessException;
	
	/**
	 * ��ѯ��Ƹ��Ա����ְ��Ϣ
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public PsnJobVO[] queryPsnJobVOs(AggRMPsndocVO aggvo) throws BusinessException;
	
	/**
	 * У��Ψһ��
	 * @param vo
	 * @return String: ��ʾ��Ϣ AggRMPsndocVO:�����Ľ��
	 * @param psndocVO
	 * @return
	 * @throws BusinessException
	 */
	public String checkUniqueInfo(RMPsndocVO psndocVO) throws BusinessException;
	
	/**
	 * ͨ������ְλ������ѯ����������Ϣ
	 * @param pk_publishjobs
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,RMPsnCPVO[]> queryPsnCPVOByJobPks(String[] pk_publishjobs) throws BusinessException;
	/**
	 * ��������ƥ�����
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public GeneralVO[] queryMatchResultByPsn(AggRMPsndocVO aggvo)throws BusinessException;
	
	/**
	 * У���Ƿ����ӦƸ�����е�ӦƸְλ
	 * @param pk_psndoc
	 * @throws BusinessException
	 */
	public void validatePsnJob(String pk_psndoc) throws BusinessException;
}
