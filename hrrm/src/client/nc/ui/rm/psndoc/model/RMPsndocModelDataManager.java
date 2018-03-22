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
	
	/** ӦƸ�Ǽ���Ա��ѯ���� */
	private FromWhereSQL applySQL;
	/** ��ѡͨ����Ա��ѯ���� */
//	private FromWhereSQL primarySQL;
	/** ��������Ա��ѯ���� */
	private FromWhereSQL interviewSQL;
	/** ¼������Ա��ѯ���� */
	private FromWhereSQL hireSQL;
	/** ��������Ա��ѯ���� */
	private FromWhereSQL checkinSQL;

	@Override
	public void refresh() {
		initModelByFromWhereSQLAndType(null);
	}

	@Override
	public void initModel() {
		// ���û��ѡ����֯
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
	 * ����ӦƸ��Ա״̬��ʼ��ģ��
	 * @param types ״̬���顣Ϊnullʱ��ʾ����
	 */
	public void initModelByFromWhereSQLAndType(Integer[] types){
		if(types == null)
			types = new Integer[] { (Integer) RMApplyStatusEnum.APPLY.value(),
					(Integer) RMApplyStatusEnum.PRIMARY.value(),
					(Integer) RMApplyStatusEnum.INTERVIEW.value(),
					(Integer) RMApplyStatusEnum.HIRE.value(),
					(Integer) RMApplyStatusEnum.CHECKIN.value() };
		// ��������map
		Map<Integer, FromWhereSQL> map = new HashMap<Integer, FromWhereSQL>();
		for(Integer type:types)
			map.put(type, getFromWhereSQLByType(type));
		// ���ú�̨��ѯ
		Map<Integer, Object[]> result = null;
		try {
			result = ((IRMPsndocQueryMaintain)NCLocator.getInstance().lookup(IRMPsndocQueryMaintain.class)).queryApplyPsnFiltPhotos(getContext(), map);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessRuntimeException(e.getMessage(), e);
		}
		// ��ʼ��model
		for(Integer type:types)
			getModels().get(type.intValue()-1).initModel(result.get(type));
	}
	
	/**
	 * ����ӦƸ��Ա״̬ȡ��ѯ����
	 * ����ѯ����Ϊ��ʱ������ new nc.ui.hr.pub.FromWhereSQL()
	 * @param type
	 * @return
	 */
	private FromWhereSQL getFromWhereSQLByType(int type){
		switch(type) {
		// ӦƸ�Ǽ���Ա
		case 1: return applySQL == null? new nc.ui.hr.pub.FromWhereSQL():applySQL;
		// ��ѡͨ����Ա
		case 2: return new nc.ui.hr.pub.FromWhereSQL();
		// ��������Ա
		case 3: return interviewSQL == null? new nc.ui.hr.pub.FromWhereSQL():interviewSQL;
		// ¼������Ա
		case 4: return hireSQL == null? new nc.ui.hr.pub.FromWhereSQL():hireSQL;
		// ��������Ա
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
