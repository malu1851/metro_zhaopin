package nc.ui.rm.psndoc.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.rm.IRMPsndocQueryMaintain;
import nc.ui.querytemplate.querytree.FromWhereSQL;
import nc.ui.uif2.model.BillManageModel;
import nc.ui.uif2.model.IAppModelDataManagerEx;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.rm.psndoc.common.RMApplyStatusEnum;
import nc.vo.uif2.LoginContext;

import org.apache.commons.lang.StringUtils;

public class RMPsndocModelDataManager implements IAppModelDataManagerEx  {

	private List<BillManageModel> models;
	private LoginContext context;
	
	/** 应聘登记人员查询条件 */
	private FromWhereSQL applySQL;
	/** 初选通过人员查询条件 */
//	private FromWhereSQL primarySQL;
	/** 面试中人员查询条件 */
	private FromWhereSQL interviewSQL;
	/** 录用中人员查询条件 */
	private FromWhereSQL hireSQL;
	/** 报到中人员查询条件 */
	private FromWhereSQL checkinSQL;

	@Override
	public void refresh() {
		initModelByFromWhereSQLAndType(null);
	}

	@Override
	public void initModel() {
		// 如果没有选择组织
		if(StringUtils.isEmpty(context.getPk_org())){
			for(BillManageModel model:models){
				model.initModel(null);
			}
			return;
		}
		applySQL = null;
		interviewSQL = null;
		hireSQL = null;
		checkinSQL = null;
		initModelByFromWhereSQLAndType(null);
	}
	
	/**
	 * 根据应聘人员状态初始化模型
	 * @param types 状态数组。为null时表示所有
	 */
	public void initModelByFromWhereSQLAndType(Integer[] types){
		if(types == null)
			types = new Integer[] { (Integer) RMApplyStatusEnum.APPLY.value(),
					(Integer) RMApplyStatusEnum.PRIMARY.value(),
					(Integer) RMApplyStatusEnum.INTERVIEW.value(),
					(Integer) RMApplyStatusEnum.HIRE.value(),
					(Integer) RMApplyStatusEnum.CHECKIN.value() };
		// 构造条件map
		Map<Integer, FromWhereSQL> map = new HashMap<Integer, FromWhereSQL>();
		for(Integer type:types)
			map.put(type, getFromWhereSQLByType(type));
		// 调用后台查询
		Map<Integer, Object[]> result = null;
		try {
			result = ((IRMPsndocQueryMaintain)NCLocator.getInstance().lookup(IRMPsndocQueryMaintain.class)).queryApplyPsnFiltPhotos(getContext(), map);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessRuntimeException(e.getMessage(), e);
		}
		// 初始化model
		for(Integer type:types)
			getModels().get(type.intValue()-1).initModel(result.get(type));
	}
	
	/**
	 * 根据应聘人员状态取查询条件
	 * 当查询条件为空时，返回 new nc.ui.hr.pub.FromWhereSQL()
	 * @param type
	 * @return
	 */
	private FromWhereSQL getFromWhereSQLByType(int type){
		switch(type) {
		// 应聘登记人员
		case 1: return applySQL == null? new nc.ui.hr.pub.FromWhereSQL():applySQL;
		// 初选通过人员
		case 2: return new nc.ui.hr.pub.FromWhereSQL();
		// 面试中人员
		case 3: return interviewSQL == null? new nc.ui.hr.pub.FromWhereSQL():interviewSQL;
		// 录用中人员
		case 4: return hireSQL == null? new nc.ui.hr.pub.FromWhereSQL():hireSQL;
		// 报到中人员
		case 5: return checkinSQL == null? new nc.ui.hr.pub.FromWhereSQL():checkinSQL;
		default: return null;
		}
	}
	
	@Override
	public void initModelBySqlWhere(String sqlWhere) {}
	@Override
	public void setShowSealDataFlag(boolean showSealDataFlag) {}
	
	public void setApplySQL(FromWhereSQL applySQL) {
		this.applySQL = applySQL;
	}
	public void setInterviewSQL(FromWhereSQL interviewSQL) {
		this.interviewSQL = interviewSQL;
	}
	public void setHireSQL(FromWhereSQL hireSQL) {
		this.hireSQL = hireSQL;
	}
	public void setCheckinSQL(FromWhereSQL checkinSQL) {
		this.checkinSQL = checkinSQL;
	}
	public List<BillManageModel> getModels() {
		return models;
	}
	public void setModels(List<BillManageModel> models) {
		this.models = models;
	}
	public LoginContext getContext() {
		return context;
	}
	public void setContext(LoginContext context) {
		this.context = context;
	}
}
