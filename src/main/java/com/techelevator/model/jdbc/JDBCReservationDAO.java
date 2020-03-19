package com.techelevator.model.jdbc;


import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.model.Reservation;
import com.techelevator.model.ReservationDAO;

public class JDBCReservationDAO implements ReservationDAO{
	private JdbcTemplate jdbcTemplate;
	
	public JDBCReservationDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	
	@Override
	public Reservation createReservation(Reservation newRes) {
		String sql = " INSERT INTO reservation(site_id,name,start_date,num_days)"
				+" VALUES(?,?,?,?) RETURNING create_date,reservation_id;";
		//Since the create date and reservation ID are created by the database 
		//we return them in the query and add them to the object
		SqlRowSet reservations = jdbcTemplate.queryForRowSet(sql,newRes.getSiteId(),newRes.getName(),
				newRes.getStartOfRes(),newRes.getDuration());
		if(reservations.next()) {
			newRes.setCreateDate(reservations.getDate("create_date").toLocalDate());
			newRes.setId(reservations.getInt("reservation_id"));
		}
		return newRes;
	}
	
	private Reservation mapRowToReservation(SqlRowSet rows) {
		Reservation r = new Reservation();
		r.setId(rows.getInt("reservation_id"));
		r.setSiteId(rows.getInt("site_id"));
		r.setName(rows.getString("name"));
		r.setStartOfRes(rows.getDate("start_date").toLocalDate());
		r.setEndDate(rows.getDate("end_date").toLocalDate());
		r.setCreateDate(rows.getDate("create_date").toLocalDate());
		r.setDuration(rows.getInt("num_days"));
		
		return r;
	}

}
