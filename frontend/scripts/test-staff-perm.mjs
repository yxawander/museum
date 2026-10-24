const base='http://127.0.0.1:8080/api'
async function api(path,{method='GET',body,token}={}){const h={};if(body!==undefined)h['Content-Type']='application/json';if(token)h.Authorization='Bearer '+token;const r=await fetch(base+path,{method,headers:h,body:body===undefined?undefined:JSON.stringify(body)});const t=await r.text();let d;try{d=JSON.parse(t)}catch{d=t};return {status:r.status,body:d}}
const login=await api('/auth/login',{method:'POST',body:{username:'staff',password:'Staff@123'}})
const token=login.body.data.token
const list=await api('/admin/reservations?page=1&size=5',{token})
const exp=await api('/admin/reservations/export',{token})
const cancel=await api('/admin/reservations/1/cancel',{method:'PUT',token,body:{reason:'x'}})
console.log('staff list',list.body.code,'export',exp.body.code,'cancel',cancel.body.code)
