create table employee (
	employee_id serial primary key,
	first_name varchar(30) not null,
	last_name varchar(30) not null
);

create table expense (
	expense_id serial primary key,
	amount bigint not null check(amount > 0),
	status varchar(8) not null,
	date bigint not null,
	issuer int
);

alter table expense add constraint exp_employee_fk foreign key(issuer) references Employee(employee_id);

create function checkExpenseStatus() 
returns trigger
language plpgsql
as $$
begin
	if (old.status != 'PENDING') then
		raise exception 'Attempt to update a non-pending expense.';
		return null;
	end if;
	if (TG_OP = 'DELETE') then
		return old;
	end if;
	return new;
end; $$



create trigger checkStatusOnUpdate before update on expense
for each row
execute function checkExpenseStatus();

create trigger checkStatusOnDelete before delete on expense
for each row 
execute function checkExpenseStatus();

drop trigger checkStatusOnUpdate on expense;
drop trigger checkStatusOnDelete on expense;
drop function checkexpensestatus;
drop table employee;
drop table expense;

insert into employee (first_name, last_name) values (
	'Not a', 'Real Employee'
);
insert into expense (amount, status, date, issuer) values (
	1234566789, 'APPROVED', 123456789, 1
);
insert into expense (amount, status, date, issuer) values (
	1234566789, 'DENIED', 123456789, 1
);

select * from employee;
select * from expense;
