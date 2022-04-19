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

select * from employee;

alter table expense add constraint exp_employee_fk foreign key(issuer) references Employee(employee_id);

--alter table expense drop column expense_amount;
--alter table expense add column expense_amount float not null check(expense_amount > 0);

create function checkExpenseStatus() 
returns trigger
language plpgsql
as $$
begin
	if (old.status != 'PENDING') then
		raise exception 'Attempt to update a non-pending expense.';
		return null;
	end if;
	return new;
end; $$

drop function checkexpensestatus;

create trigger checkStatus before update on expense
for each row
execute function checkExpenseStatus();

drop trigger checkStatus on expense;

insert into expense (amount, status, date) values (
	-100, 'PENDING', 1000
);

select * from employee;
select * from expense;