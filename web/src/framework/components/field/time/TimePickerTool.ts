import dayjs from "dayjs";


export function valueToDate(value:any, fmt:string){
  if (value != null && value != '') {
    let type = typeof value;
    if (type == 'string' || type == 'number') {
      value = dayjs(value, fmt);
    }

  }

  return value;
}
