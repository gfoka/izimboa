const { chromium } = require('playwright');
const A = process.argv[2];
(async () => {
  const b = await chromium.launch();
  const jobs = [['icon',512,512],['feature',1024,500]].concat([1,2,3,4,5,6].map(i=>['shot'+i,1080,1920]));
  for (const [n,w,h] of jobs) {
    const p = await b.newPage({ viewport: { width: w, height: h } });
    await p.goto('file://' + A + '/' + n + '.html'); await p.waitForTimeout(300);
    await p.screenshot({ path: A + '/out/' + n + '.png' }); await p.close();
  }
  await b.close();
})();
