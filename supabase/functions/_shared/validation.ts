export function text(value:unknown,min=1,max=500):string|null{if(typeof value!=='string')return null;const v=value.trim();return v.length>=min&&v.length<=max?v:null;}
export function positiveInt(value:unknown,min=1,max=100):number|null{const n=Number(value);return Number.isInteger(n)&&n>=min&&n<=max?n:null;}
export function coord(value:unknown,min:number,max:number):number|null{const n=Number(value);return Number.isFinite(n)&&n>=min&&n<=max?n:null;}
