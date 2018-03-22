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
 * 应聘人员查询接口
 * @author yucheng
 *
 */
public interface IRMPsndocQueryMaintain {

	/**
	 * 通过条件查询应聘人员
	 * @param context
	 * @param fromWhereSQL
	 * @param psnType 类型：应聘登记、初选通过、面试中、录用中、报到中
	 * @return
	 * @throws BusinessException
	 */
	public Map<Integer, Object[]> queryApplyPsns(LoginContext context, Map<Integer, FromWhereSQL> map) throws BusinessException;
	/**
	 * 通过条件查询应聘人员（不包含人员的图片）
	 * @param context
	 * @param map
	 * @return
	 * @throws BusinessException
	 */
	public Map<Integer, Object[]> queryApplyPsnFiltPhotos(LoginContext context, Map<Integer, FromWhereSQL> map)throws BusinessException;
	
	/**
	 * 查询指定人员推荐的应聘人员信息
	 * @param pk_psndoc
	 * @param fromWhereSQL
	 * @return
	 * @throws BusinessException
	 */
	public AggRMPsndocVO[] queryRecommendPsns(String pk_psndoc, FromWhereSQL fromWhereSQL) throws BusinessException;
	
	/**
	 * 通过主键查询应聘人员信息
	 * @param pk_psndoc
	 * @return
	 * @throws BusinessException
	 */
	public AggRMPsndocVO queryByPK(String pk_psndoc) throws BusinessException;
	
	public AggRMPsndocVO queryByPK(String pk_org ,String pk_psndoc) throws BusinessException;
	
	/**
	 * 查询再聘人员
	 * @param pk_org
	 * @return
	 * @throws BusinessException
	 */
	public AggRMPsndocVO[] queryReApplyPsns(String pk_org) throws BusinessException;
	
	/**
	 * 查询再聘人员曾任职信息
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public PsnJobVO[] queryPsnJobVOs(AggRMPsndocVO aggvo) throws BusinessException;
	
	/**
	 * 校验唯一性
	 * @param vo
	 * @return String: 提示信息 AggRMPsndocVO:保存后的结果
	 * @param psndocVO
	 * @return
	 * @throws BusinessException
	 */
	public String checkUniqueInfo(RMPsndocVO psndocVO) throws BusinessException;
	
	/**
	 * 通过发布职位主键查询能力素质信息
	 * @param pk_publishjobs
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,RMPsnCPVO[]> queryPsnCPVOByJobPks(String[] pk_publishjobs) throws BusinessException;
	/**
	 * 能力素质匹配分析
	 * @param aggvo
	 * @return
	 * @throws BusinessException
	 */
	public GeneralVO[] queryMatchResultByPsn(AggRMPsndocVO aggvo)throws BusinessException;
	
	/**
	 * 校验是否存在应聘流程中的应聘职位
	 * @param pk_psndoc
	 * @throws BusinessException
	 */
	public void validatePsnJob(String pk_psndoc) throws BusinessException;
}
