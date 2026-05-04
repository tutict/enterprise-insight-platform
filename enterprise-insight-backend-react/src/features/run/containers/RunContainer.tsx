import { useFlowData } from '../hooks/useFlowData'
import { useRunPage } from '../hooks/useRunPage'
import RunWorkspace from '../components/RunWorkspace'

export default function RunContainer() {
  const runPage = useRunPage()
  const workflow = useFlowData()

  return <RunWorkspace {...runPage} workflow={workflow} />
}
