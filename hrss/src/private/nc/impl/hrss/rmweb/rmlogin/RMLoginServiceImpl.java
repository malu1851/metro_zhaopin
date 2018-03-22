/*     */package nc.impl.hrss.rmweb.rmlogin;

/*     */
/*     */import java.math.BigDecimal;
/*     */
import java.util.List;
/*     */
import java.util.Map;
/*     */
import nc.bs.dao.BaseDAO;
/*     */
import nc.bs.dao.DAOException;
/*     */
import nc.itf.hrss.rmweb.rmlogin.IRMLoginService;
/*     */
import nc.jdbc.framework.SQLParameter;
/*     */
import nc.jdbc.framework.processor.BeanListProcessor;
/*     */
import nc.jdbc.framework.processor.MapProcessor;
/*     */
import nc.vo.hrss.pub.rmweb.RmUserVO;
/*     */
import nc.vo.org.OrgVO;
/*     */
import nc.vo.pub.BusinessException;
/*     */
import nc.vo.pub.lang.UFDouble;

/*     */
/*     */public class RMLoginServiceImpl implements IRMLoginService
/*     */{
	/* 19 */private BaseDAO baseDAO = new BaseDAO();

	/*     */
	/*     */public RMLoginServiceImpl() {
	}

	/*     */
	/* 23 */public String insertVO(RmUserVO vo) throws DAOException {
		return this.baseDAO.insertVO(vo);
	}

	/*     */
	/*     */public int updateVO(RmUserVO vo)
	/*     */throws DAOException
	/*     */{
		/* 28 */return this.baseDAO.updateVO(vo);
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public RmUserVO loginRMWeb(String idcard, String password)
	/*     */throws BusinessException
	/*     */{
		/* 37 */String sql = "select * from hrss_rm_user where idcard = ? and password = ?";
		/* 38 */SQLParameter param = new SQLParameter();
		/* 39 */param.addParam(idcard);
		/* 40 */param.addParam(password);
		/*     */
		/* 42 */List<RmUserVO> list = (List) this.baseDAO.executeQuery(sql,
				param, new BeanListProcessor(RmUserVO.class));
		/* 43 */return (list == null) || (list.size() == 0) ? null
				: (RmUserVO) list.get(0);
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */public UFDouble checkEMail(String email)
	/*     */throws BusinessException
	/*     */{
		/* 51 */String sql = "select count(pk_hrss_rm_user) as users from hrss_rm_user where email = ?";
		/* 52 */SQLParameter param = new SQLParameter();
		/* 53 */param.addParam(email);
		/* 54 */Map remap = (Map) this.baseDAO.executeQuery(sql, param,
				new MapProcessor());
		/* 55 */if (remap.isEmpty())
			/* 56 */return null;
		/* 57 */Object value = remap.get("users");
		/* 58 */if ((value instanceof Integer))
			/* 59 */return new UFDouble(((Integer) value).doubleValue());
		/* 60 */if ((value instanceof BigDecimal))
			/* 61 */return new UFDouble(((BigDecimal) value).doubleValue());
		/* 62 */return null;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public UFDouble checkIdCard(String idCard)
	/*     */throws BusinessException
	/*     */{
		/* 71 */String sql = "select count(pk_hrss_rm_user) as users from hrss_rm_user where idcard = ?";
		/* 72 */SQLParameter param = new SQLParameter();
		/* 73 */param.addParam(idCard);
		/* 74 */Map remap = (Map) this.baseDAO.executeQuery(sql, param,
				new MapProcessor());
		/* 75 */if (remap.isEmpty())
			/* 76 */return null;
		/* 77 */Object value = remap.get("users");
		/* 78 */if ((value instanceof Integer))
			/* 79 */return new UFDouble(((Integer) value).doubleValue());
		/* 80 */if ((value instanceof BigDecimal))
			/* 81 */return new UFDouble(((BigDecimal) value).doubleValue());
		/* 82 */return null;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public UFDouble checkHrrmpsndoc(String hrrmpsndoc, String pk_reg_job)
	/*     */throws BusinessException
	/*     */{
		/* 91 */String sql = "select count(pk_psndoc_job) as jobs from rm_psndoc_job where pk_psndoc = ? and pk_reg_job = ?";
		/* 92 */SQLParameter param = new SQLParameter();
		/* 93 */param.addParam(hrrmpsndoc);
		/* 94 */param.addParam(pk_reg_job);
		/* 95 */Map remap = (Map) this.baseDAO.executeQuery(sql, param,
				new MapProcessor());
		/* 96 */if (remap.isEmpty())
			/* 97 */return null;
		/* 98 */Object value = remap.get("jobs");
		/* 99 */if ((value instanceof Integer))
			/* 100 */return new UFDouble(((Integer) value).doubleValue());
		/* 101 */if ((value instanceof BigDecimal))
			/* 102 */return new UFDouble(((BigDecimal) value).doubleValue());
		/* 103 */return null;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */public UFDouble checkPsndocJob(String hrrmpsndoc)
	/*     */throws BusinessException
	/*     */{
		/* 111 */String sql = "select count(pk_psndoc_job) as jobs from rm_psndoc_job where sourcetype = '0'  and applystatus = '0' and pk_psndoc = ?";
		/* 112 */SQLParameter param = new SQLParameter();
		/* 113 */param.addParam(hrrmpsndoc);
		/* 114 */Map remap = (Map) this.baseDAO.executeQuery(sql, param,
				new MapProcessor());
		/* 115 */if (remap.isEmpty())
			/* 116 */return null;
		/* 117 */Object value = remap.get("jobs");
		/* 118 */if ((value instanceof Integer))
			/* 119 */return new UFDouble(((Integer) value).doubleValue());
		/* 120 */if ((value instanceof BigDecimal))
			/* 121 */return new UFDouble(((BigDecimal) value).doubleValue());
		/* 122 */return null;
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public OrgVO checkJobOrg(String hrrmpsndoc)
	/*     */throws BusinessException
	/*     */{
		/* 135 */String sql = "select * from org_orgs join rm_psndoc_job on org_orgs.pk_org = rm_psndoc_job.pk_org where rm_psndoc_job.Pk_reg_job <> '0000Z700000000000000' and pk_psndoc = ?";
		/*     */
		/* 137 */SQLParameter param = new SQLParameter();
		/* 138 */param.addParam(hrrmpsndoc);
		/* 139 */List<OrgVO> list = (List) this.baseDAO.executeQuery(sql,
				param, new BeanListProcessor(OrgVO.class));
		/* 140 */return (list == null) || (list.size() == 0) ? null
				: (OrgVO) list.get(0);
		/*     */}

	/*     */
	/*     */
	/*     */
	/*     */
	/*     */
	/*     */public RmUserVO qryRmUserVOByEmail(String email)
	/*     */throws BusinessException
	/*     */{
		/* 150 */String sql = "select * from hrss_rm_user where email = ? ";
		/* 151 */SQLParameter param = new SQLParameter();
		/* 152 */param.addParam(email);
		/*     */
		/* 154 */List<RmUserVO> list = (List) this.baseDAO.executeQuery(sql,
				param, new BeanListProcessor(RmUserVO.class));
		/* 155 */return (list == null) || (list.size() == 0) ? null
				: (RmUserVO) list.get(0);
		/*     */}

	public RmUserVO qryRmUserVOByIdcard(String idcard)
	/*     */throws BusinessException
	/*     */{
		/* 150 */String sql = "select * from hrss_rm_user where idcard = ? ";
		/* 151 */SQLParameter param = new SQLParameter();
		/* 152 */param.addParam(idcard);
		/*     */
		/* 154 */List<RmUserVO> list = (List) this.baseDAO.executeQuery(sql,
				param, new BeanListProcessor(RmUserVO.class));
		/* 155 */return (list == null) || (list.size() == 0) ? null
				: (RmUserVO) list.get(0);
		/*     */}
	/*     */
}

/*
 * Location: D:\nchome_jc\modules\hrss\META-INF\classes Qualified Name:
 * nc.impl.hrss.rmweb.rmlogin.RMLoginServiceImpl Java Class Version: 6 (50.0)
 * JD-Core Version: 0.7.0.1
 */